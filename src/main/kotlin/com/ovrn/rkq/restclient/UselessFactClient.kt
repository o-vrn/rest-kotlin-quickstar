package com.ovrn.rkq.restclient

import com.ovrn.rkq.model.UselessFactDto
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/api/v2/facts/")
@RegisterRestClient(configKey = "useless-api")
interface UselessFactClient {
    @GET
    @Path("/random")
    fun getRandomFact(): Uni<UselessFactDto?>
}