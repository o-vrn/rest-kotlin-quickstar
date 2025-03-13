package com.ovrn.rkq.config

import com.ovrn.rkq.model.Fact
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Named
import jakarta.ws.rs.Produces
import java.util.concurrent.ConcurrentHashMap

@ApplicationScoped
class ApplicationConfig {

    @Produces
    @ApplicationScoped
    @Named("factCache")
    fun getFactCache(): MutableMap<String, Fact> = ConcurrentHashMap()

    @Produces
    @ApplicationScoped
    @Named("factStatisticStore")
    fun getFactStatisticsStore(): MutableMap<String, Int> = ConcurrentHashMap()
}