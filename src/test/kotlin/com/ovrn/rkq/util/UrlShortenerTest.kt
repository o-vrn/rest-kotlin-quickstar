package com.ovrn.rkq.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class UrlShortenerTest {
    @ParameterizedTest
    @CsvSource(
        value = [
            "daf2c54fc64779e07467ea7453400849, 05b1jf6",
            "98748f512d649ecb20e289c22c11fb45, nhyu16"
        ]
    )
    fun testCompress(input: String, expected: String) {
        val result = UrlShortener.compress(input)
        assertEquals(expected, result)
    }
}