package com.ovrn.rkq.resource

import com.ovrn.rkq.model.FactStatisticDto
import com.ovrn.rkq.service.FactAccessStatisticService
import io.mockk.every
import io.quarkiverse.test.junit.mockk.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.smallrye.mutiny.Uni
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test

@QuarkusTest
class AdminResourceTest {
    @InjectMock
    private lateinit var factAccessStatisticService: FactAccessStatisticService

    @Test
    fun testGetStatisticsEndpoint() {
        val fact1 = FactStatisticDto("1", 11)
        val fact2 = FactStatisticDto("2", 12)

        val facts = listOf(fact1, fact2)
        every { factAccessStatisticService.getAllStatistics() } returns facts
            .let { Uni.createFrom().item(it) }

        given().`when`().get("/admin/statistics")
            .then()
            .statusCode(200)
            .body(
                "size()", `is`(2),
                "[0].shortened_url", `is`(fact1.id),
                "[0].access_count", `is`(fact1.accessCount),
                "[1].shortened_url", `is`(fact2.id),
                "[1].access_count", `is`(fact2.accessCount)
            )
    }

    @Test
    fun `Test getStatistics endpoint with empty data`() {
        every { factAccessStatisticService.getAllStatistics() } returns listOf<FactStatisticDto>()
            .let { Uni.createFrom().item(it) }

        given().`when`().get("/admin/statistics")
            .then()
            .statusCode(204)
            .body(Matchers.emptyOrNullString())
    }
}