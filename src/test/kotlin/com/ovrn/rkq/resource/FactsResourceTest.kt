package com.ovrn.rkq.resource

import com.ovrn.rkq.model.RandomFactDto
import com.ovrn.rkq.restclient.UselessFactClient
import com.ovrn.rkq.service.FactCache
import com.ovrn.rkq.util.GET_FACT_METRIC_NAME
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.quarkiverse.test.junit.mockk.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


@QuarkusTest
class FactsResourceTest {
    @InjectMock
    @RestClient
    private lateinit var uselessFactClient: UselessFactClient

    @Inject
    private lateinit var factCache: FactCache

    @Inject
    private lateinit var registry: MeterRegistry

    @BeforeEach
    fun preTest() {
        factCache.clear().await().indefinitely()
        registry.find(GET_FACT_METRIC_NAME).counters().forEach(registry::remove)
    }

    @Test
    fun testGetRandomEndpoint() {
        val randomFactDto = RandomFactDto(
            id = "1",
            text = "Fact from Quarkus REST",
            source = "test",
            sourceUrl = "http://example.com",
            language = "en",
            permalink = "http://example.com/permalink"
        )
        val listOf = listOf(randomFactDto)

        every { uselessFactClient.getRandomFact() } returnsMany listOf
            .map { Uni.createFrom().item(it) }

        val factId = randomFactDto.id
        given().`when`().post("/facts")
            .then()
            .statusCode(200)
            .body(
                "shortened_url", `is`(factId),
                "original_fact", `is`(randomFactDto.text)
            )

        factCache.getFact(factId)
            .subscribe()
            .with(
                { Assertions.assertEquals(randomFactDto, it) },
                { Assertions.fail("Can't get fact from cache") }
            )

    }

    @Test
    fun testGetCachedFactEndpoint() {
        val randomFactDto = RandomFactDto(
            id = "1",
            text = "Fact from Quarkus REST",
            source = "test",
            sourceUrl = "http://example.com",
            language = "en",
            permalink = "http://example.com/permalink"
        )
        val listOf = listOf(randomFactDto)

        every { uselessFactClient.getRandomFact() } returnsMany listOf
            .map { Uni.createFrom().item(it) }

        val factId = randomFactDto.id
        given().`when`().post("/facts")
            .then()
            .statusCode(200)
            .body(
                "shortened_url", `is`(factId),
                "original_fact", `is`(randomFactDto.text)
            )

        given().`when`().get("/facts/${factId}")
            .then()
            .statusCode(200)
            .body(
                "fact", `is`(randomFactDto.text),
                "original_permalink", `is`(randomFactDto.permalink)
            )
    }

    @Test
    fun testGetAllCachedFactsEndpoint() {
        val randomFactDto1 = RandomFactDto(
            id = "1",
            text = "Fact from Quarkus REST",
            source = "test",
            sourceUrl = "http://example.com",
            language = "en",
            permalink = "http://example.com/permalink"
        )
        val randomFactDto2 = RandomFactDto(
            id = "2",
            text = "Another fact from Quarkus REST",
            source = "test",
            sourceUrl = "http://example.com/2",
            language = "en",
            permalink = "http://example.com/permalink/2"
        )
        val listOf = listOf(randomFactDto1, randomFactDto2)

        every { uselessFactClient.getRandomFact() } returnsMany listOf
            .map { Uni.createFrom().item(it) }

        listOf.forEach {
            given().`when`().post("/facts")
                .then()
                .statusCode(200)
                .body(
                    "shortened_url", `is`(it.id),
                    "original_fact", `is`(it.text)
                )
        }

        given().`when`().get("/facts")
            .then()
            .statusCode(200)
            .body(
                "size()", `is`(2),
                "[0].fact", `is`(randomFactDto1.text),
                "[1].fact", `is`(randomFactDto2.text)
            )

    }

    @Test
    fun testFactOriginalLinkEndpoint() {
        val randomFactDto = RandomFactDto(
            id = "1",
            text = "Fact from Quarkus REST",
            source = "test",
            sourceUrl = "http://example.com",
            language = "en",
            permalink = "http://example.com/permalink"
        )
        val listOf = listOf(randomFactDto)

        every { uselessFactClient.getRandomFact() } returnsMany listOf
            .map { Uni.createFrom().item(it) }

        val factId = randomFactDto.id
        given().`when`().post("/facts")
            .then()
            .statusCode(200)
            .body(
                "shortened_url", `is`(factId),
                "original_fact", `is`(randomFactDto.text)
            )

        given().redirects()
            .follow(false).`when`().get("/facts/${factId}/redirect")
            .then()
            .statusCode(303)
            .header("Location", `is`(randomFactDto.permalink))
    }

    @Test
    fun testMetricCountEndpoint() {
        val randomFactDto = RandomFactDto(
            id = "1",
            text = "Fact from Quarkus REST",
            source = "test",
            sourceUrl = "http://example.com",
            language = "en",
            permalink = "http://example.com/permalink"
        )
        val listOf = listOf(randomFactDto)

        every { uselessFactClient.getRandomFact() } returnsMany listOf
            .map { Uni.createFrom().item(it) }

        val factId = randomFactDto.id
        given().`when`().post("/facts")
            .then()
            .statusCode(200)
            .body(
                "shortened_url", `is`(factId),
                "original_fact", `is`(randomFactDto.text)
            )

        given().redirects()
            .follow(false).`when`().get("/facts/${factId}/redirect")
            .then()
            .statusCode(303)
            .header("Location", `is`(randomFactDto.permalink))

        given().`when`().get("/facts/${factId}")
            .then()
            .statusCode(200)
            .body(
                "fact", `is`(randomFactDto.text),
                "original_permalink", `is`(randomFactDto.permalink)
            )
        given().`when`().get("/facts")
            .then()
            .statusCode(200)
            .body(
                "size()", `is`(1),
                "[0].fact", `is`(randomFactDto.text)
            )

        Assertions.assertEquals(2.0, registry.counter(GET_FACT_METRIC_NAME, "id", randomFactDto.id).count())
    }
}