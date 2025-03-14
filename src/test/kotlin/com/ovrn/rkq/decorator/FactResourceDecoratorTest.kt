package com.ovrn.rkq.decorator

import com.ovrn.rkq.model.FactDto
import com.ovrn.rkq.model.FactViewDto
import com.ovrn.rkq.resource.FactsResource
import com.ovrn.rkq.service.FactAccessStatisticService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber
import org.jboss.resteasy.reactive.RestResponse
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import java.net.URI

class FactResourceDecoratorTest {
    @MockK
    private lateinit var factsResource: FactsResource

    @MockK
    private lateinit var factAccessStatisticService: FactAccessStatisticService

    private lateinit var factResourceDecorator: FactResourceDecorator

    @BeforeEach
    fun init() {
        factResourceDecorator = FactResourceDecorator(factsResource, factAccessStatisticService)
    }

    @Test
    fun testGetRandomFact() {
        val expected = FactDto("Some text", "1")
        every { factsResource.getRandomFact() } returns expected
            .let { Uni.createFrom().item(it) }

        val assertSubscriber = factResourceDecorator.getRandomFact().subscribe().withSubscriber(UniAssertSubscriber.create())
        assertSubscriber.assertCompleted().assertItem(expected)
    }

    @Test
    fun testGetFact() {
        val factId = "1"
        val expected = FactViewDto("Some text", "https://example.com/permalink")
        every { factsResource.getFact(factId) } returns expected
            .let { Uni.createFrom().item(it) }
        every { factAccessStatisticService.increment(factId) } answers { Uni.createFrom().item(Unit) }

        val assertSubscriber = factResourceDecorator.getFact(factId).subscribe().withSubscriber(UniAssertSubscriber.create())

        assertSubscriber.assertCompleted().assertItem(expected)
    }

    @Test
    fun testGetAll() {
        val expected = listOf(FactViewDto("Some text", "https://example.com/permalink"))
        every { factsResource.getAll() } returns expected
            .let { Uni.createFrom().item(it) }

        val assertSubscriber = factResourceDecorator.getAll().subscribe().withSubscriber(UniAssertSubscriber.create())

        assertSubscriber.assertCompleted().assertItem(expected)
    }

    @Test
    fun testGetFactOriginalLink() {
        val factId = "1"
        val expected = RestResponse.seeOther<Any>(URI("https://example.com/permalink"))
        every { factsResource.getFactOriginalLink(factId) } returns expected
            .let { Uni.createFrom().item(it) }
        every { factAccessStatisticService.increment(factId) } answers { Uni.createFrom().item(Unit) }

        val assertSubscriber = factResourceDecorator.getFactOriginalLink(factId).subscribe().withSubscriber(UniAssertSubscriber.create())

        assertSubscriber.assertCompleted().assertItem(expected)
    }
}