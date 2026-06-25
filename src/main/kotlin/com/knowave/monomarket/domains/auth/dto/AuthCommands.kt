package com.knowave.monomarket.domains.auth.dto

import com.knowave.monomarket.common.enum.SocialProvider

data class SocialLoginCommand(
    val provider: SocialProvider,
    val token: String,
    val deviceId: String,
    val deviceName: String? = null,
)

data class RefreshTokenCommand(
    val refreshToken: String,
    val deviceId: String,
)
