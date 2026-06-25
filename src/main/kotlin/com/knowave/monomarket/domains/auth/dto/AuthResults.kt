package com.knowave.monomarket.domains.auth.dto

data class SocialLoginResult(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean,
)

data class RefreshTokenResult(
    val accessToken: String,
)
