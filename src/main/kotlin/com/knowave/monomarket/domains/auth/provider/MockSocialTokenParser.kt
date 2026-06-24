package com.knowave.monomarket.domains.auth.provider

import com.knowave.monomarket.common.enum.SocialProvider
import com.knowave.monomarket.common.exception.MonomarketException
import com.knowave.monomarket.domains.auth.dto.SocialUserInfo
import org.springframework.http.HttpStatus

internal object MockSocialTokenParser {
    fun verify(provider: SocialProvider, token: String): SocialUserInfo {
        val providerUserId = token
            .takeIf { it.startsWith(MOCK_TOKEN_PREFIX) }
            ?.removePrefix(MOCK_TOKEN_PREFIX)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw MonomarketException(
                errorCode = "INVALID_SOCIAL_TOKEN",
                message = "Invalid social token.",
                status = HttpStatus.UNAUTHORIZED,
            )

        return SocialUserInfo(
            provider = provider,
            providerUserId = providerUserId,
            email = null,
            nickname = null,
            profileImageUrl = null,
        )
    }

    private const val MOCK_TOKEN_PREFIX = "mock:"
}
