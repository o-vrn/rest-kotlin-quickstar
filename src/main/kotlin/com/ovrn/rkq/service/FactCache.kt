package com.ovrn.rkq.service

import com.ovrn.rkq.model.RandomFactDto
import io.quarkus.cache.Cache
import io.quarkus.cache.CacheName
import io.quarkus.cache.CaffeineCache
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.concurrent.CompletableFuture


@ApplicationScoped
class FactCache {
    @Inject
    @CacheName("fact-cache")
    private lateinit var cache: Cache

    fun addFact(randomFactDto: RandomFactDto) {
        cache.`as`(CaffeineCache::class.java).put(randomFactDto.id, CompletableFuture.completedFuture(randomFactDto))
    }

    fun getFact(id: String): Uni<RandomFactDto> {
        return cache.getAsync(id) {
                throw RuntimeException("Fact with ID: $id not found in cache")
            }
    }
}