package com.knowave.monomarket.domains.auth.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "monomarket.jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpirationMinutes: Long = 60,
    val refreshTokenExpirationDays: Long = 30,
)
