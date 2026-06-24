package com.knowave.monomarket.domains.auth.jwt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest
class JwtProviderTests @Autowired constructor(
    private val jwtProvider: JwtProvider,
) {
    @Test
    fun `generate and parse access token`() {
        val userId = UUID.randomUUID()

        val token = jwtProvider.generateAccessToken(userId)

        assertTrue(jwtProvider.validateToken(token))
        assertEquals(userId, jwtProvider.extractUserId(token))
        assertEquals(JwtTokenType.ACCESS, jwtProvider.extractTokenType(token))
    }

    @Test
    fun `generate and parse refresh token`() {
        val userId = UUID.randomUUID()

        val token = jwtProvider.generateRefreshToken(userId)

        assertTrue(jwtProvider.validateToken(token))
        assertEquals(userId, jwtProvider.extractUserId(token))
        assertEquals(JwtTokenType.REFRESH, jwtProvider.extractTokenType(token))
    }
}
