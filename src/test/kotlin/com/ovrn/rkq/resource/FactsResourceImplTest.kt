package com.ovrn.rkq.resource

import com.ovrn.rkq.model.Fact
import com.ovrn.rkq.service.FactService
import io.mockk.every
import io.quarkiverse.test.junit.mockk.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.smallrye.mutiny.Uni
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test

@QuarkusTest
class FactsResourceImplTest {
    @InjectMock
    private lateinit var factService: FactService

    @Test
    fun `Test getRandomFact endpoint`() {
        val fact = Fact(
            id = "1",
            text = "Fact from Quarkus REST",
            permalink = "https://example.com/permalink"
        )

        every { factService.getRandomFact() } returns fact
            .let { Uni.createFrom().item(it) }

        val factId = fact.id
        given().`when`().post("/facts")
            .then()
            .statusCode(200)
            .body(
                "shortened_url", `is`(factId),
                "original_fact", `is`(fact.text)
            )
    }

    @Test
    fun `Test getRandomFact endpoint on error from FactService`() {
        val errorMessage = "Some error"
        every { factService.getRandomFact() } returns RuntimeException(errorMessage)
            .let { Uni.createFrom().failure(it) }

        given().`when`().post("/facts")
            .then()
            .statusCode(500)
            .body("message", `is`(errorMessage))
    }

    @Test
    fun `Test getFact endpoint`() {
        val fact = Fact(
            id = "1",
            text = "Fact from Quarkus REST",
            permalink = "https://example.com/permalink"
        )
        val factId = fact.id
        every { factService.getFact(factId) } returns fact
            .let { Uni.createFrom().item(it) }

        given().`when`().get("/facts/$factId")
            .then()
            .statusCode(200)
            .body(
                "fact", `is`(fact.text),
                "original_permalink", `is`(fact.permalink)
            )
    }

    @Test
    fun `Test getFact endpoint on no fact found`() {
        val factId = "1"
        every { factService.getFact(factId) } answers { Uni.createFrom().nullItem() }

        given().`when`().get("/facts/$factId")
            .then()
            .statusCode(404)
            .body(Matchers.emptyOrNullString())
    }

    @Test
    fun `Test getAll endpoint`() {
        val fact1 = Fact(
            id = "1",
            text = "Fact from Quarkus REST",
            permalink = "https://example.com/permalink"
        )
        val fact2 = Fact(
            id = "2",
            text = "Another fact from Quarkus REST",
            permalink = "https://example.com/permalink2"
        )
        val facts = listOf(fact1, fact2)

        every { factService.getAll() } returns facts
            .let { Uni.createFrom().item(it) }

        given().`when`().get("/facts")
            .then()
            .statusCode(200)
            .body(
                "size()", `is`(2),
                "[0].original_permalink", `is`(fact1.permalink),
                "[0].fact", `is`(fact1.text),
                "[1].original_permalink", `is`(fact2.permalink),
                "[1].fact", `is`(fact2.text)
            )
    }

    @Test
    fun `Test getAll endpoint on no fact in the list`() {
        every { factService.getAll() } returns listOf<Fact>()
            .let { Uni.createFrom().item(it) }

        given().`when`().get("/facts")
            .then()
            .statusCode(204)
            .body(Matchers.emptyOrNullString())
    }

    @Test
    fun `Test getFactOriginalLink endpoint`() {
        val fact = Fact(
            id = "1",
            text = "Fact from Quarkus REST",
            permalink = "https://example.com/permalink"
        )

        val factId = fact.id
        every { factService.getFact(factId) } returns fact
            .let { Uni.createFrom().item(it) }

        given().redirects()
            .follow(false).`when`().get("/facts/$factId/redirect")
            .then()
            .statusCode(303)
            .header("Location", `is`(fact.permalink))
    }

    @Test
    fun `Test getFactOriginalLink endpoint on no fact found`() {
        val factId = "1"
        every { factService.getFact(factId) } answers { Uni.createFrom().nullItem() }

        given().`when`().get("/facts/$factId/redirect")
            .then()
            .statusCode(404)
            .body(Matchers.emptyOrNullString())
    }
}