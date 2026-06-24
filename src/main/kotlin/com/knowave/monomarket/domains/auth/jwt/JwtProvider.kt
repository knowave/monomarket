package com.knowave.monomarket.domains.auth.jwt

import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Component
class JwtProvider(
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder,
    private val jwtProperties: JwtProperties,
) {
    fun generateAccessToken(userId: UUID): String {
        return generateToken(
            userId = userId,
            tokenType = JwtTokenType.ACCESS,
            expiresAt = Instant.now().plus(jwtProperties.accessTokenExpirationMinutes, ChronoUnit.MINUTES),
        ).token
    }

    fun generateRefreshToken(userId: UUID): String {
        return issueRefreshToken(userId).token
    }

    fun issueRefreshToken(userId: UUID): IssuedJwtToken {
        val expiresAt = Instant.now().plus(jwtProperties.refreshTokenExpirationDays, ChronoUnit.DAYS)
        return generateToken(
            userId = userId,
            tokenType = JwtTokenType.REFRESH,
            expiresAt = expiresAt,
        )
    }

    fun validateToken(token: String): Boolean {
        return try {
            jwtDecoder.decode(token)
            true
        } catch (exception: JwtException) {
            false
        } catch (exception: IllegalArgumentException) {
            false
        }
    }

    fun extractUserId(token: String): UUID {
        val jwt = jwtDecoder.decode(token)
        val userId = requireNotNull(jwt.getClaimAsString(USER_ID_CLAIM)) {
            "JWT userId claim is required."
        }
        return UUID.fromString(userId)
    }

    fun extractTokenType(token: String): JwtTokenType {
        val jwt = jwtDecoder.decode(token)
        val tokenType = requireNotNull(jwt.getClaimAsString(TOKEN_TYPE_CLAIM)) {
            "JWT tokenType claim is required."
        }
        return JwtTokenType.valueOf(tokenType)
    }

    fun isRefreshToken(token: String): Boolean {
        return runCatching { extractTokenType(token) == JwtTokenType.REFRESH }
            .getOrDefault(false)
    }

    private fun generateToken(
        userId: UUID,
        tokenType: JwtTokenType,
        expiresAt: Instant,
    ): IssuedJwtToken {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(userId.toString())
            .id(UUID.randomUUID().toString())
            .claim(USER_ID_CLAIM, userId.toString())
            .claim(TOKEN_TYPE_CLAIM, tokenType.name)
            .build()
        val headers = JwsHeader.with(MacAlgorithm.HS256).build()

        return IssuedJwtToken(
            token = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).tokenValue,
            expiresAt = expiresAt,
        )
    }

    companion object {
        private const val USER_ID_CLAIM = "userId"
        private const val TOKEN_TYPE_CLAIM = "tokenType"
    }
}
