package com.ovrn.rkq.resource

import com.ovrn.rkq.model.FactStatisticDto
import com.ovrn.rkq.service.FactAccessStatisticService
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/admin")
class AdminResource(private val factAccessStatisticService: FactAccessStatisticService) {
    @GET
    @Path("/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    fun getStatistics(): Uni<List<FactStatisticDto>?> {
        return factAccessStatisticService.getAllStatistics().map { list ->
            when {
                list.isEmpty() -> return@map null
                else -> list
            }
        }
    }
}