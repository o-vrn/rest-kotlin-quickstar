package com.ovrn.rkq.resource

import com.ovrn.rkq.model.FactDto
import com.ovrn.rkq.model.FactViewDto
import com.ovrn.rkq.model.RandomFactDto
import com.ovrn.rkq.restclient.UselessFactClient
import com.ovrn.rkq.service.FactCache
import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.resteasy.reactive.RestResponse
import java.net.URI


@Path("/facts")
class FactsResource(@RestClient private val uselessFactClient: UselessFactClient, private val factCache: FactCache) {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    fun getRandomFact(): Uni<FactDto> {
        return uselessFactClient.getRandomFact()
            .onFailure()
                .recoverWithUni { throwable ->
                    val message = "Failed to retrieve response from Useless Fact Client"
                    Log.error(message, throwable)
                    Uni.createFrom().failure(InternalServerErrorException(message, throwable))
                }
            .onItem()
                .ifNull()
                .failWith {
                    val message = "Useless Fact Client returned an empty response"
                    Log.warn(message)
                    InternalServerErrorException(message)
                }
            .onItem()
                .ifNotNull()
                .call(factCache::addFact)
                .map { FactDto(it.text, it.id) }
    }

    @GET
    @Path("/{shortened_url}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getFact(@PathParam("shortened_url") id: String): Uni<FactViewDto> {
        return getCachedFact(id).map { FactViewDto(it.text, it.permalink) }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAll(): Uni<List<FactViewDto>> {
        return factCache.getAll().map { list -> list.map { FactViewDto(it.text, it.permalink) } }
    }

    @GET
    @Path("/{shortened_url}/redirect")
    fun getFactOriginalLink(@PathParam("shortened_url") id: String): Uni<RestResponse<Any>> {
        return getCachedFact(id).map { RestResponse.seeOther(URI.create(it.permalink)) }
    }

    private fun getCachedFact(id: String): Uni<RandomFactDto> = factCache.getFact(id)
        .onFailure()
            .recoverWithUni { throwable -> Uni.createFrom().failure(BadRequestException(throwable.message)) }
}