package com.knowave.monomarket.domains.auth.controller

import com.knowave.monomarket.domains.auth.dto.RefreshTokenRequest
import com.knowave.monomarket.domains.auth.dto.RefreshTokenResponse
import com.knowave.monomarket.domains.auth.dto.SocialLoginRequest
import com.knowave.monomarket.domains.auth.dto.SocialLoginResponse
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
        return authService.socialLogin(request)
    }

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshTokenRequest,
    ): RefreshTokenResponse {
        return authService.refresh(request)
    }
}
