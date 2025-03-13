package com.ovrn.rkq.util

import jakarta.ws.rs.InternalServerErrorException
import jakarta.ws.rs.core.Response

class WebException {
    companion object {
        fun internal(message: String?, throwable: Throwable): InternalServerErrorException {
            val errorMessage = message ?: "Internal Server Error"
            return InternalServerErrorException(
                errorMessage,
                Response.status(500).entity(WebExceptionDto(errorMessage)).build(),
                throwable
            )
        }
    }

    private data class WebExceptionDto(
        val message: String
    )
}
