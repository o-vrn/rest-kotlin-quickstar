package com.ovrn.rkq.resource

import com.ovrn.rkq.model.RandomFactDto
import com.ovrn.rkq.restclient.UselessFactClient
import io.mockk.every
import io.quarkiverse.test.junit.mockk.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test


@QuarkusTest
class FactsResourceTest {
    @InjectMock
    @RestClient
    private lateinit var uselessFactClient: UselessFactClient

    @Test
    fun testHelloEndpoint() {
        every { uselessFactClient.getRandomFact() } returns Uni.createFrom().item(
            RandomFactDto(
                id = "1",
                text = "Fact from Quarkus REST",
                source = "test",
                source_url = "http://example.com",
                language = "en",
                permalink = "http://example.com/permalink"
            )
        )

        given()
            .`when`().get("/facts")
            .then()
            .statusCode(200)
            .body(
                "shortened_url", `is`("1"),
                "original_fact", `is`("Fact from Quarkus REST")
            )
    }

}