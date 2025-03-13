package com.ovrn.rkq.config

import com.ovrn.rkq.model.Fact
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Named
import jakarta.inject.Singleton
import jakarta.ws.rs.Produces
import java.util.concurrent.ConcurrentHashMap

@ApplicationScoped
class ApplicationConfig {

    @Produces
    @Singleton
    @Named("factCache")
    fun getFactCache(): MutableMap<String, Fact> = ConcurrentHashMap()

    @Produces
    @Singleton
    @Named("factStatisticStore")
    fun getFactStatisticsStore(): MutableMap<String, Int> = ConcurrentHashMap()
}