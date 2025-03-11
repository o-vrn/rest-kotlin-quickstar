package com.ovrn.rkq.resource

import com.ovrn.rkq.model.RandomFactDto
import com.ovrn.rkq.restclient.UselessFactClient
import io.mockk.every
import io.quarkiverse.test.junit.mockk.InjectMock
import io.quarkus.cache.Cache
import io.quarkus.cache.CacheName
import io.quarkus.cache.CaffeineCache
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test


@QuarkusTest
class FactsResourceTest {
    @InjectMock
    @RestClient
    private lateinit var uselessFactClient: UselessFactClient

    @Inject
    @CacheName("fact-cache")
    private lateinit var cache: Cache

    @Test
    fun testHelloEndpoint() {
        val randomFactDto = RandomFactDto(
            id = "1",
            text = "Fact from Quarkus REST",
            source = "test",
            source_url = "http://example.com",
            language = "en",
            permalink = "http://example.com/permalink"
        )
        every { uselessFactClient.getRandomFact() } returns Uni.createFrom().item(
            randomFactDto
        )

        val factId = randomFactDto.id
        given()
            .`when`().post("/facts")
            .then()
            .statusCode(200)
            .body(
                "shortened_url", `is`(factId),
                "original_fact", `is`(randomFactDto.text)
            )

        cache.`as`(CaffeineCache::class.java)
            .getIfPresent<RandomFactDto>(factId)
            .get()
            .let { assert(it == randomFactDto) }
    }

}