package com.ovrn.rkq.model

import com.fasterxml.jackson.annotation.JsonProperty

data class FactViewDto(
    @JsonProperty("fact")
    val text: String,
    @JsonProperty("original_permalink")
    val permalink: String
)
