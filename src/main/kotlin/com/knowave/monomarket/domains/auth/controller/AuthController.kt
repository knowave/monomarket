package com.knowave.monomarket.domains.auth.controller

import com.knowave.monomarket.domains.auth.dto.RefreshTokenCommand
import com.knowave.monomarket.domains.auth.dto.RefreshTokenRequest
import com.knowave.monomarket.domains.auth.dto.RefreshTokenResponse
import com.knowave.monomarket.domains.auth.dto.RefreshTokenResult
import com.knowave.monomarket.domains.auth.dto.SocialLoginCommand
import com.knowave.monomarket.domains.auth.dto.SocialLoginRequest
import com.knowave.monomarket.domains.auth.dto.SocialLoginResponse
import com.knowave.monomarket.domains.auth.dto.SocialLoginResult
import com.knowave.monomarket.domains.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/social-login")
    fun socialLogin(
        @Valid @RequestBody request: SocialLoginRequest,
    ): SocialLoginResponse {
        return authService.socialLogin(request.toCommand()).toResponse()
    }

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshTokenRequest,
    ): RefreshTokenResponse {
        return authService.refresh(request.toCommand()).toResponse()
    }

    private fun SocialLoginRequest.toCommand(): SocialLoginCommand {
        return SocialLoginCommand(
            provider = provider,
            token = token,
            deviceId = deviceId,
            deviceName = deviceName,
        )
    }

    private fun RefreshTokenRequest.toCommand(): RefreshTokenCommand {
        return RefreshTokenCommand(
            refreshToken = refreshToken,
            deviceId = deviceId,
        )
    }

    private fun SocialLoginResult.toResponse(): SocialLoginResponse {
        return SocialLoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            isNewUser = isNewUser,
        )
    }

    private fun RefreshTokenResult.toResponse(): RefreshTokenResponse {
        return RefreshTokenResponse(accessToken = accessToken)
    }
}
