package com.ovrn.rkq.resource

import com.ovrn.rkq.model.RandomFactDto
import com.ovrn.rkq.restclient.UselessFactClient
import com.ovrn.rkq.service.FactCache
import com.ovrn.rkq.util.GET_FACT_COUNT_METRIC
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.quarkiverse.test.junit.mockk.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


@QuarkusTest
class AdminResourceTest {
    @InjectMock
    @RestClient
    private lateinit var uselessFactClient: UselessFactClient

    @Inject
    private lateinit var registry: MeterRegistry

    @Inject
    private lateinit var factCache: FactCache

    @BeforeEach
    fun preTest() {
        factCache.clear().await().indefinitely()
        registry.find(GET_FACT_COUNT_METRIC).counters().forEach(registry::remove)
    }

    @Test
    fun testGetStatisticsEndpoint() {
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

        given().`when`().get("/admin/statistics")
            .then()
            .statusCode(200)
            .body(
                "size()", `is`(0)
            )

        given().redirects()
            .follow(false).`when`().get("/facts/${randomFactDto1.id}/redirect")
            .then()
            .statusCode(303)

        given().`when`().get("/facts/${randomFactDto1.id}")
            .then()
            .statusCode(200)

        given().`when`().get("/facts/${randomFactDto2.id}")
            .then()
            .statusCode(200)

        val missingFactId = "3"
        given().`when`().get("/facts/${missingFactId}")
            .then()
            .statusCode(400)
            .body("message", `is`("Fact with ID: 3 not found in cache"))

        given().`when`().get("/facts")
            .then()
            .statusCode(200)

        given().`when`().get("/admin/statistics")
            .then()
            .statusCode(200)
            .body(
                "size()", `is`(3),
                "[0].access_count", `is`(2.0F),
                "[0].shortened_url", `is`(randomFactDto1.id),
                "[1].access_count", `is`(1.0F),
                "[1].shortened_url", `is`(randomFactDto2.id),
                "[2].access_count", `is`(1.0F),
                "[2].shortened_url", `is`(missingFactId)
            )
    }
}