package com.ovrn.rkq.service

import com.ovrn.rkq.model.RandomFactDto
import com.ovrn.rkq.util.GET_FACT_METRIC_NAME
import io.micrometer.core.instrument.MeterRegistry
import io.quarkus.cache.Cache
import io.quarkus.cache.CacheName
import io.quarkus.cache.CaffeineCache
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.concurrent.CompletableFuture


@ApplicationScoped
class FactCacheImpl : FactCache {
    @Inject
    private lateinit var registry: MeterRegistry

    @Inject
    @CacheName("fact-cache")
    private lateinit var cache: Cache

    override fun addFact(randomFactDto: RandomFactDto) {
        cache.`as`(CaffeineCache::class.java).put(randomFactDto.id, CompletableFuture.completedFuture(randomFactDto))
    }

    override fun getFact(id: String): Uni<RandomFactDto> {
        registry.counter(GET_FACT_METRIC_NAME, "id", id).increment()
        return cache.getAsync(id) {
            throw RuntimeException("Fact with ID: $id not found in cache")
        }
    }

    override fun getAll(): Uni<List<RandomFactDto>> {
        val uniList = cache.`as`(CaffeineCache::class.java)
            .keySet()
            .map { cache.getAsync<String, RandomFactDto>(it as String?) { null } as Uni<RandomFactDto> }

        return Uni.combine().all().unis<List<RandomFactDto>>(uniList)
            .with { it.filterIsInstance<RandomFactDto>() }
    }

    override fun clear(): Uni<Void> {
        return cache.invalidateAll()
    }
}