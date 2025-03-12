package com.ovrn.rkq.util

import com.ovrn.rkq.model.WebExceptionDto
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.InternalServerErrorException
import jakarta.ws.rs.core.Response

class WebException {
    companion object {
        fun badRequest(message: String?): BadRequestException {
            val response = Response.status(400).entity(message?.let { WebExceptionDto(it) }).build()
            return BadRequestException(message, response)
        }

        fun internal(message: String, throwable: Throwable): InternalServerErrorException {
            val response = Response.status(500).entity(WebExceptionDto(message)).build()
            return InternalServerErrorException(message, response, throwable)
        }

        fun internal(message: String): InternalServerErrorException {
            val response = Response.status(500).entity(WebExceptionDto(message)).build()
            return InternalServerErrorException(message, response)
        }
    }
}
