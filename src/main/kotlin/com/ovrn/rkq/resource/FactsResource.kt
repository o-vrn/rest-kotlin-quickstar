package com.ovrn.rkq.resource

import com.ovrn.rkq.model.FactDto
import com.ovrn.rkq.restclient.UselessFactClient
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RestClient


@Path("/facts")
class FactsResource {
    @RestClient
    lateinit var extensionsService: UselessFactClient

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun randomFact(): Uni<FactDto> {
        return extensionsService.getRandomFact()
            .onItem()
            .ifNull()
            .failWith(Exception("No random fact found"))
            .onItem()
            .transform { randomFactDto -> FactDto(randomFactDto.text, randomFactDto.id) }
    }
}