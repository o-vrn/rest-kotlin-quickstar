package com.ovrn.rkq.service

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
        factAccessStatisticService.increment(factId)
        factAccessStatisticService.increment(factId)

        assertEquals(1, store.size)
        assertEquals(2, store[factId])
    }

    @Test
    fun getAllStatistics() {
        val factId1 = "1"
        val factId2 = "2"
        store[factId1] = 2
        store[factId2] = 1

        val allStatistics = factAccessStatisticService.getAllStatistics().await().indefinitely()

        assertEquals(2, allStatistics.size)
        assertEquals(factId1, allStatistics[0].id)
        assertEquals(2, allStatistics[0].accessCount)
        assertEquals(factId2, allStatistics[1].id)
        assertEquals(1, allStatistics[1].accessCount)
    }
}