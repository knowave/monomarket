package com.knowave.monomarket.domains.auth.dto

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank
    val refreshToken: String,

    @field:NotBlank
    val deviceId: String,
)
