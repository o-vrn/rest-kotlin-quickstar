package com.ovrn.rkq.model

import com.fasterxml.jackson.annotation.JsonProperty

data class RandomFactDto(
    val id: String,
    val text: String,
    val source: String,
    @JsonProperty("source_url")
    val sourceUrl: String,
    val language: String,
    val permalink: String
)
