package com.ovrn.rkq.service

import com.ovrn.rkq.model.Fact
import com.ovrn.rkq.restclient.UselessFactClient
import com.ovrn.rkq.util.UrlShortener
import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Named
import org.eclipse.microprofile.rest.client.inject.RestClient

@ApplicationScoped
class FactServiceImpl(
    @RestClient private val uselessFactClient: UselessFactClient,
    @Named("factCache") private val cache: MutableMap<String, Fact>
) : FactService {

    override fun getRandomFact(): Uni<Fact> {
        return uselessFactClient.getRandomFact()
            .onFailure()
            .transform { throwable ->
                val message = "Failed to retrieve response from Useless Fact Client"
                Log.error(message, throwable)
                RuntimeException(message, throwable)
            }
            .onItem().ifNull().failWith({
                val message = "Useless Fact Client returned an empty response"
                Log.warn(message)
                RuntimeException(message)
            })
            .onItem()
            .transform {
                val idCompressed = UrlShortener.compress(it!!.id)
                cache.compute(idCompressed) { _, existingFact ->
                    if (existingFact != null) return@compute existingFact
                    Fact(idCompressed, it.text, it.permalink)
                }
            }
    }

    override fun getFact(id: String): Uni<Fact?> {
        return Uni.createFrom().item { cache[id] }
    }

    override fun getAll(): Uni<List<Fact>> {
        return Uni.createFrom().item { cache.values.toList() }
    }
}