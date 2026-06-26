package com.knowave.monomarket.domains.user.controller

import com.knowave.monomarket.domains.auth.principal.CustomUserPrincipal
import com.knowave.monomarket.domains.user.dto.DeleteMyAccountCommand
import com.knowave.monomarket.domains.user.dto.GetUserProfileResponse
import com.knowave.monomarket.domains.user.dto.GetUserProfileResult
import com.knowave.monomarket.domains.user.dto.UpdateNicknameCommand
import com.knowave.monomarket.domains.user.dto.UpdateNicknameRequest
import com.knowave.monomarket.domains.user.dto.UpdateNicknameResult
import com.knowave.monomarket.domains.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/me")
    fun getUserProfile(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
    ): GetUserProfileResponse {
        return userService.getUserProfile(principal.userId).toResponse()
    }

    @PatchMapping("/me/nickname")
    fun updateNickname(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @Valid @RequestBody request: UpdateNicknameRequest,
    ): GetUserProfileResponse {
        return userService.updateNickname(
            userId = principal.userId,
            command = request.toCommand(),
        ).toResponse()
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMyAccount(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
    ) {
        userService.deleteMyAccount(
            userId = principal.userId,
            command = DeleteMyAccountCommand(),
        )
    }

    private fun UpdateNicknameRequest.toCommand(): UpdateNicknameCommand {
        return UpdateNicknameCommand(nickname = nickname)
    }

    private fun GetUserProfileResult.toResponse(): GetUserProfileResponse {
        return GetUserProfileResponse(
            id = id,
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            createdAt = createdAt,
        )
    }

    private fun UpdateNicknameResult.toResponse(): GetUserProfileResponse {
        return GetUserProfileResponse(
            id = id,
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            createdAt = createdAt,
        )
    }
}
