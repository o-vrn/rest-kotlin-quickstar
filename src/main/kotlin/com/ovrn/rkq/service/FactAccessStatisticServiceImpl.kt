package com.ovrn.rkq.service

import com.ovrn.rkq.model.FactStatisticDto
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Named

@ApplicationScoped
class FactAccessStatisticServiceImpl(@Named("factStatisticStore")private val store: MutableMap<String, Int>) : FactAccessStatisticService {
    override fun increment(factId: String) {
        store.compute(factId) { _, v ->
            when (v) {
                null -> 1
                else -> v + 1
            }
        }
    }

    override fun getAllStatistics(): Uni<List<FactStatisticDto>> {
        return Uni.createFrom().item(store.map { FactStatisticDto(it.key, it.value) })
    }
}