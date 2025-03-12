package com.ovrn.rkq.service

import com.ovrn.rkq.model.RandomFactDto
import com.ovrn.rkq.util.GET_FACT_COUNT_METRIC
import com.ovrn.rkq.util.ID_TAG
import com.ovrn.rkq.util.MISSING_TAG
import io.micrometer.core.instrument.MeterRegistry
import io.quarkus.cache.Cache
import io.quarkus.cache.CacheName
import io.quarkus.cache.CaffeineCache
import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import java.util.concurrent.CompletableFuture


@ApplicationScoped
class FactCacheImpl(
    private val registry: MeterRegistry,
    @CacheName("fact-cache")
    private val cache: Cache
) : FactCache {

    override fun addFact(randomFactDto: RandomFactDto): Uni<Void> {
        return Uni.createFrom().item({
            cache.`as`(CaffeineCache::class.java)
                .put(randomFactDto.id, CompletableFuture.completedFuture(randomFactDto))
            randomFactDto
        }).invoke { fact ->
            Log.info("Added random fact with id ${fact.id} added to cache")
        }.replaceWithVoid()

    }

    override fun getFact(id: String): Uni<RandomFactDto> {
        return cache.getAsync<String?, RandomFactDto?>(id)
        {
            registry.counter(GET_FACT_COUNT_METRIC, ID_TAG, it, MISSING_TAG, true.toString()).increment()
            val message = "Fact with ID: $it not found in cache"
            Log.warn(message)
            throw RuntimeException(message)
        }
            .onItem()
            .invoke { fact -> registry.counter(GET_FACT_COUNT_METRIC, ID_TAG, fact.id).increment() }
    }

    override fun getAll(): Uni<List<RandomFactDto>> {
        val uniList = cache.`as`(CaffeineCache::class.java)
            .keySet()
            .map { cache.getAsync<String, RandomFactDto>(it as String) { null } as Uni<RandomFactDto> }

        return Uni.combine().all().unis<List<RandomFactDto>>(uniList)
            .with { it.filterIsInstance<RandomFactDto>() }
    }

    override fun clear(): Uni<Void> {
        return cache.invalidateAll()
            .invoke { _ -> Log.info("Cache cleared successfully") }
    }
}