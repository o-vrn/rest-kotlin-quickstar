package com.ovrn.rkq.service

import com.ovrn.rkq.model.FactStatisticDto
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FactAccessStatisticServiceImplTest {
    private lateinit var store: MutableMap<String, Int>
    private lateinit var factAccessStatisticService: FactAccessStatisticService

    @BeforeEach
    fun init() {
        store = mutableMapOf()
        factAccessStatisticService = FactAccessStatisticServiceImpl(store)
    }

    @Test
    fun increment() {
        val factId = "1"
        val subscriber1 = factAccessStatisticService.increment(factId).subscribe()
            .withSubscriber(UniAssertSubscriber.create())
        val subscriber2 = factAccessStatisticService.increment(factId).subscribe()
            .withSubscriber(UniAssertSubscriber.create())

        subscriber1.assertCompleted().assertItem(Unit)
        subscriber2.assertCompleted().assertItem(Unit)

        assertEquals(1, store.size)
        assertEquals(2, store[factId])
    }

    @Test
    fun getAllStatistics() {
        val expected = listOf(FactStatisticDto("1", 2), FactStatisticDto("2", 1))
        expected.forEach {
            store[it.id] = it.accessCount
        }

        val subscriber = factAccessStatisticService.getAllStatistics().subscribe().withSubscriber(UniAssertSubscriber.create())

        subscriber.assertCompleted().assertItem(expected)
    }
}