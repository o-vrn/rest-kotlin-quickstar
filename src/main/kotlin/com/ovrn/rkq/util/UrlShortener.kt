package com.ovrn.rkq.util

class UrlShortener {
    companion object {
        fun compress(id: String): String {
            // SHA-256 looks like overkill, maybe custom mapper with radix > 36
            return id.hashCode().let { Integer.toString(it, 36) }.replaceFirst("-", "0")
        }
    }
}