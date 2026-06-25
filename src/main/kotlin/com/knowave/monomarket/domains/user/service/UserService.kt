package com.knowave.monomarket.domains.user.service

import com.knowave.monomarket.common.exception.MonomarketException
import com.knowave.monomarket.common.enum.SocialProvider
import com.knowave.monomarket.common.enum.UserStatus
import com.knowave.monomarket.domains.user.dto.UserMeResult
import com.knowave.monomarket.domains.user.entity.User
import com.knowave.monomarket.domains.user.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun createSocialUser(
        provider: SocialProvider,
        providerUserId: String,
        profileImageUrl: String?,
    ): User {
        return userRepository.save(
            User(
                nickname = createDefaultNickname(provider, providerUserId),
                profileImageUrl = profileImageUrl,
            )
        )
    }

    @Transactional(readOnly = true)
    fun existsById(userId: UUID): Boolean {
        return userRepository.existsById(userId)
    }

    @Transactional(readOnly = true)
    fun existsActiveById(userId: UUID): Boolean {
        return userRepository.existsByIdAndStatus(userId, UserStatus.ACTIVE.name)
    }

    @Transactional(readOnly = true)
    fun getMe(userId: UUID): UserMeResult {
        val user = getUser(userId)

        return UserMeResult(
            id = requireNotNull(user.id),
            nickname = user.nickname,
        )
    }

    @Transactional(readOnly = true)
    fun getUser(userId: UUID): User {
        return userRepository.findById(userId).orElseThrow {
            MonomarketException(
                errorCode = "USER_NOT_FOUND",
                message = "User not found.",
                status = HttpStatus.NOT_FOUND,
            )
        }
    }

    private fun createDefaultNickname(provider: SocialProvider, providerUserId: String): String {
        val source = "${provider.name}:$providerUserId"
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray())
            .joinToString(separator = "") { "%02x".format(it) }
            .take(16)

        return "${provider.name.lowercase()}_$hash"
    }
}
