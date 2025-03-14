package com.ovrn.rkq.service

import com.ovrn.rkq.model.Fact
import com.ovrn.rkq.model.UselessFactDto
import com.ovrn.rkq.restclient.UselessFactClient
import com.ovrn.rkq.util.UrlShortener
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FactServiceImplTest {
    @MockK
    private lateinit var uselessFactClient: UselessFactClient

    private lateinit var cache: MutableMap<String, Fact>
    private lateinit var factService: FactService

    @BeforeEach
    fun init() {
        cache = mutableMapOf()
        factService = FactServiceImpl(uselessFactClient, cache)
    }

    @Test
    fun testGetRandomFact() {
        val uselessFact = UselessFactDto(
            id = "1",
            text = "Some useless fact",
            source = "Generated",
            sourceUrl = "https://source.url",
            language = "en",
            permalink = "https://some.url"
        )
        val expected = Fact(UrlShortener.compress(uselessFact.id), uselessFact.text, uselessFact.permalink)

        every { uselessFactClient.getRandomFact() } returns uselessFact
            .let { Uni.createFrom().item(it) }

        val subscriber = factService.getRandomFact().subscribe().withSubscriber(UniAssertSubscriber.create())

        subscriber.assertCompleted()
            .assertItem(expected)

        assertEquals(1, cache.size)
        assertEquals(cache[expected.id], expected)
    }

    @Test
    fun `Test getRandomFact already existing in the cache`() {
        val uselessFact = UselessFactDto(
            id = "1",
            text = "Some useless fact",
            source = "Generated",
            sourceUrl = "https://source.url",
            language = "en",
            permalink = "https://some.url"
        )
        val cachedFact = Fact(
            id = UrlShortener.compress(uselessFact.id),
            text = uselessFact.text,
            permalink = uselessFact.permalink
        )
        cache[cachedFact.id] = cachedFact

        every { uselessFactClient.getRandomFact() } returns uselessFact
            .let { Uni.createFrom().item(it) }

        val subscriber = factService.getRandomFact().subscribe().withSubscriber(UniAssertSubscriber.create())

        subscriber.assertCompleted()
            .assertItem(cachedFact)

        assertEquals(1, cache.size)
        assertEquals(cache[cachedFact.id], cachedFact)
    }

    @Test
    fun `Test getRandomFact on empty response from the UselessFactClient`() {
        every { uselessFactClient.getRandomFact() } answers { Uni.createFrom().nullItem() }

        factService.getRandomFact().subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertFailedWith(RuntimeException::class.java, "Useless Fact Client returned an empty response")
    }

    @Test
    fun `Test getRandomFact on error response from the UselessFactClient`() {
        every { uselessFactClient.getRandomFact() } answers { Uni.createFrom().failure(RuntimeException("Some error")) }

        factService.getRandomFact().subscribe().withSubscriber(UniAssertSubscriber.create())
            .assertFailedWith(RuntimeException::class.java, "Failed to retrieve response from Useless Fact Client")
    }

    @Test
    fun testGetFact() {
        val cachedFact = Fact(
            id = "1",
            text = "Some fact text",
            permalink = "https://some.url"
        )
        cache[cachedFact.id] = cachedFact

        val subscriber = factService.getFact(cachedFact.id).subscribe().withSubscriber(UniAssertSubscriber.create())

        subscriber.assertCompleted()
            .assertItem(cachedFact)
    }

    @Test
    fun testGetAll() {
        val cachedFact1 = Fact(
            id = "1",
            text = "Some fact text",
            permalink = "https://some.url"
        )
        val cachedFact2 = Fact(
            id = "2",
            text = "Some other fact text",
            permalink = "https://some.other.url"
        )
        val expected = listOf(cachedFact1, cachedFact2)
        expected.forEach { cache[it.id] = it }

        val subscriber = factService.getAll().subscribe().withSubscriber(UniAssertSubscriber.create())

        subscriber.assertCompleted()
            .assertItem(expected)
    }
}