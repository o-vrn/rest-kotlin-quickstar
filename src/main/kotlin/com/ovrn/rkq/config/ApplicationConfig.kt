package com.ovrn.rkq.config

import com.ovrn.rkq.model.Fact
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Produces
import java.util.concurrent.ConcurrentHashMap

@ApplicationScoped
class ApplicationConfig {

    @Produces
    @ApplicationScoped
    fun getFactCache(): MutableMap<String, Fact> = ConcurrentHashMap()
}