package com.knowave.monomarket.domains.auth.service

import com.knowave.monomarket.common.enum.SocialProvider
import com.knowave.monomarket.common.exception.MonomarketException
import com.knowave.monomarket.domains.auth.dto.RefreshTokenCommand
import com.knowave.monomarket.domains.auth.dto.SocialLoginCommand
import com.knowave.monomarket.domains.auth.jwt.JwtProvider
import com.knowave.monomarket.domains.auth.repository.RefreshTokenRepository
import com.knowave.monomarket.domains.user.repository.SocialAccountRepository
import com.knowave.monomarket.domains.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
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
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    @Test
    fun `social login creates user and social account by provider user id`() {
        val response = authService.socialLogin(
            SocialLoginCommand(
                provider = SocialProvider.KAKAO,
                token = "mock:123456",
                deviceId = "ios-device-1",
                deviceName = "iPhone",
            )
        )

        assertTrue(response.isNewUser)
        assertEquals(1, userRepository.count())
        assertEquals(1, socialAccountRepository.count())
        assertEquals(1, refreshTokenRepository.count())
    }

    @Test
    fun `social login returns existing user and replaces refresh token row for same device`() {
        authService.socialLogin(
            SocialLoginCommand(
                provider = SocialProvider.GOOGLE,
                token = "mock:google-user",
                deviceId = "android-device-1",
                deviceName = "Galaxy",
            )
        )

        val secondResponse = authService.socialLogin(
            SocialLoginCommand(
                provider = SocialProvider.GOOGLE,
                token = "mock:google-user",
                deviceId = "android-device-1",
                deviceName = "Galaxy renamed",
            )
        )

        assertFalse(secondResponse.isNewUser)
        assertEquals(1, userRepository.count())
        assertEquals(1, socialAccountRepository.count())
        assertEquals(1, refreshTokenRepository.count())
        assertEquals("Galaxy renamed", refreshTokenRepository.findByToken(secondResponse.refreshToken)?.deviceName)
    }

    @Test
    fun `social login creates separate refresh token rows for different devices`() {
        authService.socialLogin(
            SocialLoginCommand(
                provider = SocialProvider.GOOGLE,
                token = "mock:multi-device-user",
                deviceId = "ios-device",
            )
        )

        val secondResponse = authService.socialLogin(
            SocialLoginCommand(
                provider = SocialProvider.GOOGLE,
                token = "mock:multi-device-user",
                deviceId = "android-device",
            )
        )

        assertFalse(secondResponse.isNewUser)
        assertEquals(1, userRepository.count())
        assertEquals(1, socialAccountRepository.count())
        assertEquals(2, refreshTokenRepository.count())
    }

    @Test
    fun `social login rejects invalid provider token`() {
        assertThrows<MonomarketException> {
            authService.socialLogin(
                SocialLoginCommand(
                    provider = SocialProvider.APPLE,
                    token = "invalid-token",
                    deviceId = "ios-device-1",
                )
            )
        }
    }

    @Test
    fun `refresh issues access token and updates last used at`() {
        val loginResponse = authService.socialLogin(
            SocialLoginCommand(
                provider = SocialProvider.KAKAO,
                token = "mock:refresh-user",
                deviceId = "ios-device-1",
            )
        )

        val response = authService.refresh(
            RefreshTokenCommand(
                refreshToken = loginResponse.refreshToken,
                deviceId = "ios-device-1",
            )
        )

        assertTrue(jwtProvider.validateToken(response.accessToken))
        assertNotNull(refreshTokenRepository.findByToken(loginResponse.refreshToken)?.lastUsedAt)
    }

    @Test
    fun `refresh rejects mismatched device id`() {
        val loginResponse = authService.socialLogin(
            SocialLoginCommand(
                provider = SocialProvider.KAKAO,
                token = "mock:mismatched-device-user",
                deviceId = "ios-device-1",
            )
        )

        assertThrows<MonomarketException> {
            authService.refresh(
                RefreshTokenCommand(
                    refreshToken = loginResponse.refreshToken,
                    deviceId = "android-device-1",
                )
            )
        }
    }

    @Test
    fun `refresh rejects access token`() {
        val accessToken = jwtProvider.generateAccessToken(UUID.randomUUID())

        assertThrows<MonomarketException> {
            authService.refresh(
                RefreshTokenCommand(
                    refreshToken = accessToken,
                    deviceId = "ios-device-1",
                )
            )
        }
    }
}
