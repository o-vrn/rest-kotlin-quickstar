package com.ovrn.rkq.resource

import com.ovrn.rkq.model.FactDto
import com.ovrn.rkq.model.FactViewDto
import com.ovrn.rkq.service.FactService
import com.ovrn.rkq.util.WebException
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.*
import org.jboss.resteasy.reactive.RestResponse
import java.net.URI


@Path("/facts")
class FactsResourceImpl(private val factService: FactService) : FactsResource {

    override fun getRandomFact(): Uni<FactDto> {
        return factService.getRandomFact()
            .onFailure().transform { WebException.internal(it.message, it) }
            .onItem().transform { FactDto(it.text, it.shortenUrl) }
    }

    override fun getFact(id: String): Uni<FactViewDto?> {
        return factService.getFact(id)
            .onItem().ifNull().failWith(NotFoundException("No fact found for id $id"))
            .onItem().transform { FactViewDto(it!!.text, it.permalink) }
    }

    override fun getAll(): Uni<List<FactViewDto>?> {
        return factService.getAll().map { list ->
            if (list.isEmpty()) return@map null
            list.map { FactViewDto(it.text, it.permalink) }
        }
    }

    override fun getFactOriginalLink(id: String): Uni<RestResponse<Any>> {
        return factService.getFact(id)
            .onItem().ifNull().failWith(NotFoundException("No fact found for id $id"))
            .onItem().transform { RestResponse.seeOther(URI.create(it!!.permalink)) }
    }
}