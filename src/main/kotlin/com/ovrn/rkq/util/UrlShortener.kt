package com.ovrn.rkq.util

class UrlShortener {
    companion object {
        fun compress(id: String): String {
            return id.hashCode().toString().replaceFirst("-", "0")
        }
    }
}