package com.knowave.monomarket.domains.user.service

import com.knowave.monomarket.common.enum.SocialProvider
import com.knowave.monomarket.common.enum.UserStatus
import com.knowave.monomarket.domains.auth.service.RefreshTokenService
import com.knowave.monomarket.domains.favorite.service.FavoriteService
import com.knowave.monomarket.domains.product.service.ProductService
import com.knowave.monomarket.domains.user.dto.DeleteMyAccountCommand
import com.knowave.monomarket.domains.user.dto.GetUserProfileResult
import com.knowave.monomarket.domains.user.dto.UpdateNicknameCommand
import com.knowave.monomarket.domains.user.dto.UpdateNicknameResult
import com.knowave.monomarket.domains.user.entity.User
import com.knowave.monomarket.domains.user.exception.UserExceptions
import com.knowave.monomarket.domains.user.repository.UserRepository
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    @Lazy
    private val favoriteService: FavoriteService,
    @Lazy
    private val productService: ProductService,
    private val socialAccountService: SocialAccountService,
    private val refreshTokenService: RefreshTokenService,
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
    fun getUserProfile(userId: UUID): GetUserProfileResult {
        val user = getUser(userId)

        return user.toGetUserProfileResult()
    }

    @Transactional(readOnly = true)
    fun getUser(userId: UUID): User {
        return userRepository.findUserById(userId)
            ?: throw UserExceptions.notFound()
    }

    @Transactional
    fun updateNickname(
        userId: UUID,
        command: UpdateNicknameCommand,
    ): UpdateNicknameResult {
        val user = getUser(userId)
        val nickname = command.nickname.trim()
        validateNickname(nickname)

        val existingUser = userRepository.findUserByNickname(nickname)
        if (existingUser != null && existingUser.id != user.id) {
            throw UserExceptions.nicknameAlreadyExists()
        }

        user.nickname = nickname

        return UpdateNicknameResult(
            id = requireNotNull(user.id),
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
            createdAt = requireNotNull(user.createdAt),
        )
    }

    @Transactional
    fun deleteMyAccount(
        userId: UUID,
        command: DeleteMyAccountCommand,
    ) {
        val user = getUser(userId)

        favoriteService.deleteManyFavoriteByUser(userId)
        favoriteService.deleteManyFavoriteByProductSeller(userId)
        productService.deleteManyProductBySeller(userId)
        socialAccountService.deleteManySocialAccountByUser(userId)
        refreshTokenService.deleteManyRefreshTokenByUser(userId)
        userRepository.delete(user)
    }

    private fun createDefaultNickname(provider: SocialProvider, providerUserId: String): String {
        val source = "${provider.name}:$providerUserId"
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray())
            .joinToString(separator = "") { "%02x".format(it) }
            .take(16)

        return "${provider.name.lowercase()}_$hash"
    }

    private fun validateNickname(nickname: String) {
        if (nickname.isBlank()) {
            throw UserExceptions.invalidNickname("Nickname must not be blank.")
        }
        if (nickname.length > NICKNAME_MAX_LENGTH) {
            throw UserExceptions.invalidNickname("Nickname must be less than or equal to 50 characters.")
        }
    }

    private fun User.toGetUserProfileResult(): GetUserProfileResult {
        return GetUserProfileResult(
            id = requireNotNull(id),
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            createdAt = requireNotNull(createdAt),
        )
    }

    companion object {
        private const val NICKNAME_MAX_LENGTH = 50
    }
}
