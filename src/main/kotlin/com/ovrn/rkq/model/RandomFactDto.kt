package com.ovrn.rkq.model

data class RandomFactDto(
    val id: String,
    val text: String,
    val source: String,
    val source_url: String,
    val language: String,
    val permalink: String
)
