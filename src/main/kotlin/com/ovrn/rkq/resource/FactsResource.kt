package com.ovrn.rkq.resource

import com.ovrn.rkq.model.FactDto
import com.ovrn.rkq.model.FactViewDto
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.RestResponse

interface FactsResource {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    fun getRandomFact(): Uni<FactDto>

    @GET
    @Path("/{shortened_url}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getFact(@PathParam("shortened_url") id: String): Uni<FactViewDto?>

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAll(): Uni<List<FactViewDto>?>

    @GET
    @Path("/{shortened_url}/redirect")
    fun getFactOriginalLink(@PathParam("shortened_url") id: String): Uni<RestResponse<Any>>
}