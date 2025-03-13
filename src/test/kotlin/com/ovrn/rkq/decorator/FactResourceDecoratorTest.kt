package com.ovrn.rkq.decorator

import com.ovrn.rkq.model.FactDto
import com.ovrn.rkq.model.FactViewDto
import com.ovrn.rkq.resource.FactsResource
import com.ovrn.rkq.service.FactAccessStatisticService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.smallrye.mutiny.Uni
import org.jboss.resteasy.reactive.RestResponse
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach

class FactResourceDecoratorTest {
    @MockK
    private lateinit var factsResource: FactsResource

    @MockK
    private lateinit var factAccessStatisticService: FactAccessStatisticService

    private lateinit var facdecorator: FactResourceDecorator

    @BeforeEach
    fun init() {
        facdecorator = FactResourceDecorator(factsResource, factAccessStatisticService)
    }

    @Test
    fun getRandomFact() {
        every { factsResource.getRandomFact() } returns FactDto("Some text", "1")
            .let { Uni.createFrom().item(it) }

        facdecorator.getRandomFact()

        verify(exactly = 1) { factsResource.getRandomFact() }
    }

    @Test
    fun getFact() {
        val factId = "1"
        every { factsResource.getFact(factId) } returns FactViewDto("Some text", "http://example.com/permalink")
            .let { Uni.createFrom().item(it) }
        every { factAccessStatisticService.increment(factId) } returns Unit

        facdecorator.getFact(factId)

        verify(exactly = 1) { factsResource.getFact(factId) }
        verify(exactly = 1) { factAccessStatisticService.increment(factId) }
    }

    @Test
    fun getAll() {
        every { factsResource.getAll() } returns FactViewDto("Some text", "http://example.com/permalink")
            .let { Uni.createFrom().item(listOf(it)) }

        facdecorator.getAll()

        verify(exactly = 1) { factsResource.getAll() }
    }

    @Test
    fun getFactOriginalLink() {
        val factId = "1"
        every { factsResource.getFactOriginalLink(factId) } returns RestResponse.ok<Any>()
            .let { Uni.createFrom().item(it) }
        every { factAccessStatisticService.increment(factId) } returns Unit

        facdecorator.getFactOriginalLink(factId)

        verify(exactly = 1) { factsResource.getFactOriginalLink(factId) }
        verify(exactly = 1) { factAccessStatisticService.increment(factId) }
    }
}