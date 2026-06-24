package com.knowave.monomarket.domains.auth.service

import com.knowave.monomarket.common.enum.SocialProvider
import com.knowave.monomarket.common.exception.MonomarketException
import com.knowave.monomarket.domains.auth.dto.RefreshTokenRequest
import com.knowave.monomarket.domains.auth.dto.RefreshTokenResponse
import com.knowave.monomarket.domains.auth.dto.SocialLoginRequest
import com.knowave.monomarket.domains.auth.dto.SocialLoginResponse
import com.knowave.monomarket.domains.auth.dto.SocialUserInfo
import com.knowave.monomarket.domains.auth.entity.RefreshToken
import com.knowave.monomarket.domains.auth.jwt.JwtProvider
import com.knowave.monomarket.domains.auth.jwt.JwtTokenType
import com.knowave.monomarket.domains.auth.provider.SocialTokenVerifier
import com.knowave.monomarket.domains.auth.repository.RefreshTokenRepository
import com.knowave.monomarket.domains.user.entity.SocialAccount
import com.knowave.monomarket.domains.user.entity.User
import com.knowave.monomarket.domains.user.repository.SocialAccountRepository
import com.knowave.monomarket.domains.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class AuthService(
    private val socialTokenVerifiers: List<SocialTokenVerifier>,
    private val socialAccountRepository: SocialAccountRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userService: UserService,
    private val jwtProvider: JwtProvider,
) {
    @Transactional
    fun socialLogin(request: SocialLoginRequest): SocialLoginResponse {
        val socialUserInfo = selectVerifier(request.provider).verify(request.token)
        if (socialUserInfo.provider != request.provider) {
            throw MonomarketException(
                errorCode = "SOCIAL_PROVIDER_MISMATCH",
                message = "Social provider does not match verified token.",
                status = HttpStatus.UNAUTHORIZED,
            )
        }

        val socialAccount = socialAccountRepository.findByProviderAndProviderUserId(
            provider = socialUserInfo.provider.name,
            providerUserId = socialUserInfo.providerUserId,
        )

        val user: User
        val isNewUser: Boolean
        if (socialAccount != null) {
            user = socialAccount.user
            isNewUser = false
        } else {
            user = createUserWithSocialAccount(socialUserInfo)
            isNewUser = true
        }

        val userId = requireNotNull(user.id) { "User id must be generated before issuing JWT." }
        val refreshToken = jwtProvider.issueRefreshToken(userId)
        saveRefreshToken(
            user = user,
            token = refreshToken.token,
            deviceId = request.deviceId,
            deviceName = request.deviceName,
            expiresAt = LocalDateTime.ofInstant(refreshToken.expiresAt, ZoneId.systemDefault()),
        )

        return SocialLoginResponse(
            accessToken = jwtProvider.generateAccessToken(userId),
            refreshToken = refreshToken.token,
            isNewUser = isNewUser,
        )
    }

    @Transactional(readOnly = true)
    fun refresh(request: RefreshTokenRequest): RefreshTokenResponse {
        if (!jwtProvider.validateToken(request.refreshToken)) {
            throw invalidRefreshTokenException()
        }

        val tokenType = jwtProvider.extractTokenType(request.refreshToken)
        if (tokenType != JwtTokenType.REFRESH) {
            throw invalidRefreshTokenException()
        }

        val userId = jwtProvider.extractUserId(request.refreshToken)
        val refreshToken = refreshTokenRepository.findByToken(request.refreshToken)
            ?: throw invalidRefreshTokenException()
        if (refreshToken.user.id != userId) {
            throw invalidRefreshTokenException()
        }
        if (refreshToken.deviceId != request.deviceId) {
            throw invalidRefreshTokenException()
        }
        if (refreshToken.revokedAt != null) {
            throw invalidRefreshTokenException()
        }
        if (refreshToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw invalidRefreshTokenException()
        }
        if (refreshToken.token != request.refreshToken) {
            throw invalidRefreshTokenException()
        }
        if (!userService.existsActiveById(userId)) {
            throw invalidRefreshTokenException()
        }

        refreshToken.markUsed(LocalDateTime.now())

        return RefreshTokenResponse(accessToken = jwtProvider.generateAccessToken(userId))
    }

    private fun selectVerifier(provider: SocialProvider): SocialTokenVerifier {
        return socialTokenVerifiers.firstOrNull { it.supports(provider) }
            ?: throw MonomarketException(
                errorCode = "UNSUPPORTED_SOCIAL_PROVIDER",
                message = "Unsupported social provider.",
                status = HttpStatus.BAD_REQUEST,
            )
    }

    private fun createUserWithSocialAccount(socialUserInfo: SocialUserInfo): User {
        val user = userService.createSocialUser(
            provider = socialUserInfo.provider,
            providerUserId = socialUserInfo.providerUserId,
            profileImageUrl = socialUserInfo.profileImageUrl,
        )
        socialAccountRepository.save(
            SocialAccount(
                user = user,
                provider = socialUserInfo.provider.name,
                providerUserId = socialUserInfo.providerUserId,
                email = socialUserInfo.email,
            )
        )

        return user
    }

    private fun saveRefreshToken(
        user: User,
        token: String,
        deviceId: String,
        deviceName: String?,
        expiresAt: LocalDateTime,
    ) {
        val userId = requireNotNull(user.id) { "User id must be generated before saving refresh token." }
        val refreshToken = refreshTokenRepository.findByUserIdAndDeviceId(
            userId = userId,
            deviceId = deviceId,
        )

        if (refreshToken == null) {
            refreshTokenRepository.save(
                RefreshToken(
                    user = user,
                    token = token,
                    deviceId = deviceId,
                    deviceName = deviceName,
                    expiresAt = expiresAt,
                )
            )
        } else {
            refreshToken.replaceToken(
                token = token,
                expiresAt = expiresAt,
                deviceName = deviceName,
            )
        }
    }

    private fun invalidRefreshTokenException(): MonomarketException {
        return MonomarketException(
            errorCode = "INVALID_REFRESH_TOKEN",
            message = "Invalid refresh token.",
            status = HttpStatus.UNAUTHORIZED,
        )
    }
}
