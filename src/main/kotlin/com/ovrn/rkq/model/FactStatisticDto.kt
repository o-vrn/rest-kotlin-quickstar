package com.ovrn.rkq.model

import com.fasterxml.jackson.annotation.JsonProperty

data class FactStatisticDto(
    @JsonProperty("access_count")
    val count: Double,
    @JsonProperty("shortened_url")
    val id: String
)
