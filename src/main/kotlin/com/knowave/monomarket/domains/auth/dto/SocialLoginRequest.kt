package com.knowave.monomarket.domains.auth.dto

import com.knowave.monomarket.common.enum.SocialProvider
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SocialLoginRequest(
    @field:NotNull
    val provider: SocialProvider,

    @field:NotBlank
    val token: String,
)
