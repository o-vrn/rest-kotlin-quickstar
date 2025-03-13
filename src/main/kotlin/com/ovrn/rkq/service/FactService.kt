package com.ovrn.rkq.service

import com.ovrn.rkq.model.Fact
import io.smallrye.mutiny.Uni

interface FactService {
    fun getRandomFact(): Uni<Fact>
    fun getFact(id: String): Uni<Fact?>
    fun getAll(): Uni<List<Fact>>
}