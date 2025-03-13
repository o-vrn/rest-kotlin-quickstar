package com.ovrn.rkq.decorator

import com.ovrn.rkq.model.FactDto
import com.ovrn.rkq.model.FactViewDto
import com.ovrn.rkq.resource.FactsResource
import com.ovrn.rkq.service.FactAccessStatisticService
import io.smallrye.mutiny.Uni
import jakarta.annotation.Priority
import jakarta.decorator.Decorator
import jakarta.decorator.Delegate
import org.jboss.resteasy.reactive.RestResponse

@Priority(10)
@Decorator
open class FactResourceDecorator(
    @Delegate private val delegate: FactsResource,
    private val factAccessStatisticService: FactAccessStatisticService
) : FactsResource {
    override fun getRandomFact(): Uni<FactDto> {
        return delegate.getRandomFact()
    }

    override fun getFact(id: String): Uni<FactViewDto?> {
        factAccessStatisticService.increment(id)
        return delegate.getFact(id)
    }

    override fun getAll(): Uni<List<FactViewDto>?> {
        return delegate.getAll()
    }

    override fun getFactOriginalLink(id: String): Uni<RestResponse<Any>> {
        factAccessStatisticService.increment(id)
        return delegate.getFactOriginalLink(id)
    }
}