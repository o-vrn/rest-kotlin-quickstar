package com.ovrn.rkq.service

import com.ovrn.rkq.model.FactStatisticDto
import io.smallrye.mutiny.Uni

interface FactAccessStatisticService {
    fun increment(factId: String)
    fun getAllStatistics(): Uni<List<FactStatisticDto>>
}