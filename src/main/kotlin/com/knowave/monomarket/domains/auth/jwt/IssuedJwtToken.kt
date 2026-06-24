package com.knowave.monomarket.domains.auth.jwt

import java.time.Instant

data class IssuedJwtToken(
    val token: String,
    val expiresAt: Instant,
)
