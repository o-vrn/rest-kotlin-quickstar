package com.ovrn.rkq.service

import com.ovrn.rkq.model.RandomFactDto
import io.smallrye.mutiny.Uni

interface FactCache {
    fun addFact(randomFactDto: RandomFactDto): Uni<Void>
    fun getFact(id: String): Uni<RandomFactDto>
    fun getAll(): Uni<List<RandomFactDto>>
    fun clear(): Uni<Void>
}