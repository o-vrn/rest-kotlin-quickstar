package com.ovrn.rkq.resource

import com.ovrn.rkq.model.FactStatisticDto
import com.ovrn.rkq.util.GET_FACT_COUNT_METRIC
import com.ovrn.rkq.util.ID_TAG
import com.ovrn.rkq.util.WebException
import io.micrometer.core.instrument.MeterRegistry
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType


@Path("/admin")
class AdminResource(private val registry: MeterRegistry) {

    @GET
    @Path("/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    fun getStatistics(): Uni<List<FactStatisticDto>> {
        return Uni.createFrom().item(
            registry.find(GET_FACT_COUNT_METRIC)
                .tagKeys(ID_TAG)
                .counters()
                .fold(HashMap<String, Double>()) { acc, counter ->
                    counter.id.getTag(ID_TAG)
                        ?.let {
                            acc.compute(it) { _, value -> value
                                ?.let { it + counter.count() } ?: counter.count()
                            }
                        }
                    acc
                }
                .map { FactStatisticDto(it.value, it.key) }
        ).onFailure()
            .transform { WebException.internal("Can't retrieve statistics", it) }
    }

}