package com.ovrn.rkq.model

import com.fasterxml.jackson.annotation.JsonProperty

data class FactStatisticDto(
    @JsonProperty("shortened_url")
    val id: String,
    @JsonProperty("access_count")
    val accessCount: Int
)
