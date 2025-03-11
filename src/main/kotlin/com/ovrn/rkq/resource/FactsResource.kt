package com.ovrn.rkq.resource

import com.ovrn.rkq.model.FactDto
import com.ovrn.rkq.model.FactViewDto
import com.ovrn.rkq.restclient.UselessFactClient
import com.ovrn.rkq.service.FactCache
import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RestClient


@Path("/facts")
class FactsResource {
    @RestClient
    private lateinit var extensionsService: UselessFactClient

    @Inject
    private lateinit var factCache: FactCache

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    fun getRandomFact(): Uni<FactDto> {
        return extensionsService.getRandomFact()
            .onItem()
            .ifNull()
            .failWith(Exception("No random fact found"))
            .onItem()
            .invoke(factCache::addFact)
            .map { randomFactDto ->
                FactDto(randomFactDto.text, randomFactDto.id)
            }
    }

    @GET
    @Path("/{shortened_url}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getFact(@PathParam("shortened_url") id: String): Uni<FactViewDto> {
        return factCache.getFact(id)
            .map { randomFact ->
                FactViewDto(randomFact.text, randomFact.permalink)
            }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAll(): Uni<List<FactViewDto>> {
        return factCache.getAll()
            .map { list -> list.map { fact -> FactViewDto(fact.text, fact.permalink) } }
    }
}