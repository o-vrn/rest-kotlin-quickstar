package com.ovrn.rkq.model

import com.fasterxml.jackson.annotation.JsonProperty

data class FactDto(
    @JsonProperty("original_fact")
    val text: String,
    @JsonProperty("shortened_url")
    val id: String
)
