package com.ovrn.rkq.service

import com.ovrn.rkq.model.Fact
import com.ovrn.rkq.model.UselessFactDto
import com.ovrn.rkq.restclient.UselessFactClient
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.smallrye.mutiny.Uni
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
    fun getRandomFact() {
        val uselessFact = UselessFactDto(
            id = "1",
            text = "Some useless fact",
            source = "Generated",
            sourceUrl = "http://source.url",
            language = "en",
            permalink = "http://some.url"
        )

        every { uselessFactClient.getRandomFact() } returns uselessFact
            .let { Uni.createFrom().item(it) }

        val result = factService.getRandomFact().await().indefinitely()

        assertNotNull(result)
        assertEquals(uselessFact.text, result!!.text)
        assertEquals(uselessFact.id, result.id)
        assertEquals(uselessFact.permalink, result.permalink)

        assertEquals(1, cache.size)
        assertEquals(cache[result.id], result)
    }

    @Test
    fun getRandomFactExistingInCache() {
        val uselessFact = UselessFactDto(
            id = "1",
            text = "Some useless fact",
            source = "Generated",
            sourceUrl = "http://source.url",
            language = "en",
            permalink = "http://some.url"
        )
        val cachedFact = Fact(
            id = uselessFact.id,
            text = uselessFact.text,
            permalink = uselessFact.permalink
        )
        cache[cachedFact.id] = cachedFact

        every { uselessFactClient.getRandomFact() } returns uselessFact
            .let { Uni.createFrom().item(it) }

        val result = factService.getRandomFact().await().indefinitely()

        assertNotNull(result)
        assertEquals(uselessFact.text, result!!.text)
        assertEquals(uselessFact.id, result.id)
        assertEquals(uselessFact.permalink, result.permalink)

        assertEquals(1, cache.size)
        assertEquals(cache[result.id], cachedFact)
    }

    @Test
    fun getRandomFactOnEmpty() {
        every { uselessFactClient.getRandomFact() } answers { Uni.createFrom().nullItem() }

        val exception = assertThrows<RuntimeException> { factService.getRandomFact().await().indefinitely() }
        assertEquals(exception.message, "Useless Fact Client returned an empty response")
    }

    @Test
    fun getRandomFactOnClientError() {
        every { uselessFactClient.getRandomFact() } answers { Uni.createFrom().failure(RuntimeException("Some error")) }

        val exception = assertThrows<RuntimeException> { factService.getRandomFact().await().indefinitely() }
        assertEquals(exception.message, "Failed to retrieve response from Useless Fact Client")
    }

    @Test
    fun getFact() {
        val fact = Fact(
            id = "1",
            text = "Some fact text",
            permalink = "http://some.url"
        )
        cache[fact.id] = fact
        val result = factService.getFact(fact.id).await().indefinitely()

        assertNotNull(result)
        assertEquals(result!!.text, fact.text)
        assertEquals(result.id, fact.id)
        assertEquals(result.permalink, fact.permalink)
    }

    @Test
    fun getAll() {
        val fact1 = Fact(
            id = "1",
            text = "Some fact text",
            permalink = "http://some.url"
        )
        val fact2 = Fact(
            id = "2",
            text = "Some other fact text",
            permalink = "http://some.other.url"
        )
        cache[fact1.id] = fact1
        cache[fact2.id] = fact2
        val result = factService.getAll().await().indefinitely()

        assertEquals(result.size, 2)
        assertEquals(result[0], fact1)
        assertEquals(result[1], fact2)
    }
}