package com.ovrn.rkq.resource

import com.ovrn.rkq.model.FactDto
import com.ovrn.rkq.model.FactViewDto
import com.ovrn.rkq.service.FactService
import com.ovrn.rkq.util.WebException
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.RestResponse
import java.net.URI


@Path("/facts")
class FactsResource(private val factService: FactService) {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    fun getRandomFact(): Uni<FactDto?> {
        return factService.getRandomFact()
            .onFailure().transform { WebException.internal(it.message, it) }
            .onItem().transform {
                if (it == null) return@transform null
                FactDto(it.text, it.id)
            }
    }

    @GET
    @Path("/{shortened_url}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getFact(@PathParam("shortened_url") id: String): Uni<FactViewDto?> {
        return factService.getFact(id)
            .onItem().ifNull().failWith(NotFoundException("No fact found for id $id"))
            .onItem().transform { it?.let { FactViewDto(it.text, it.permalink) } }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAll(): Uni<List<FactViewDto>?> {
        return factService.getAll().map { list ->
            if (list.isEmpty()) return@map null
            list.map { FactViewDto(it.text, it.permalink) }
        }
    }

    @GET
    @Path("/{shortened_url}/redirect")
    fun getFactOriginalLink(@PathParam("shortened_url") id: String): Uni<RestResponse<Any>> {
        return factService.getFact(id)
            .onItem().ifNull().failWith(NotFoundException("No fact found for id $id"))
            .onItem().transform { it?.let { RestResponse.seeOther(URI.create(it.permalink)) } }
    }
}