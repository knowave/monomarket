package com.knowave.monomarket.domains.user.controller

import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import com.knowave.monomarket.domains.user.dto.UserMeResponse
import com.knowave.monomarket.domains.user.dto.UserMeResult
import com.knowave.monomarket.domains.user.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
    ): UserMeResponse {
        return userService.getMe(principal.userId).toResponse()
    }

    private fun UserMeResult.toResponse(): UserMeResponse {
        return UserMeResponse(
            id = id,
            nickname = nickname,
        )
    }
}
