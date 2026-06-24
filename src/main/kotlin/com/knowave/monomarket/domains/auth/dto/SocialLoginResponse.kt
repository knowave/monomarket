package com.knowave.monomarket.domains.auth.dto

data class SocialLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean,
)
