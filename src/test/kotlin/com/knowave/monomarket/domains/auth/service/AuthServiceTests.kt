package com.knowave.monomarket.domains.auth.service

import com.knowave.monomarket.common.enum.SocialProvider
import com.knowave.monomarket.common.exception.MonomarketException
import com.knowave.monomarket.domains.auth.dto.RefreshTokenRequest
import com.knowave.monomarket.domains.auth.dto.SocialLoginRequest
import com.knowave.monomarket.domains.auth.jwt.JwtProvider
import com.knowave.monomarket.domains.user.repository.SocialAccountRepository
import com.knowave.monomarket.domains.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@Transactional
class AuthServiceTests @Autowired constructor(
    private val authService: AuthService,
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val socialAccountRepository: SocialAccountRepository,
) {
    @Test
    fun `social login creates user and social account by provider user id`() {
        val response = authService.socialLogin(
            SocialLoginRequest(
                provider = SocialProvider.KAKAO,
                token = "mock:123456",
            )
        )

        assertTrue(response.isNewUser)
        assertEquals(1, userRepository.count())
        assertEquals(1, socialAccountRepository.count())
    }

    @Test
    fun `social login returns existing user for same provider and provider user id`() {
        authService.socialLogin(
            SocialLoginRequest(
                provider = SocialProvider.GOOGLE,
                token = "mock:google-user",
            )
        )

        val secondResponse = authService.socialLogin(
            SocialLoginRequest(
                provider = SocialProvider.GOOGLE,
                token = "mock:google-user",
            )
        )

        assertFalse(secondResponse.isNewUser)
        assertEquals(1, userRepository.count())
        assertEquals(1, socialAccountRepository.count())
    }

    @Test
    fun `social login rejects invalid provider token`() {
        assertThrows<MonomarketException> {
            authService.socialLogin(
                SocialLoginRequest(
                    provider = SocialProvider.APPLE,
                    token = "invalid-token",
                )
            )
        }
    }

    @Test
    fun `refresh rejects access token`() {
        val accessToken = jwtProvider.generateAccessToken(UUID.randomUUID())

        assertThrows<MonomarketException> {
            authService.refresh(RefreshTokenRequest(refreshToken = accessToken))
        }
    }
}
