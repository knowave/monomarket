package com.knowave.monomarket.domains.user.service

import com.knowave.monomarket.domains.user.entity.SocialAccount
import com.knowave.monomarket.domains.user.entity.User
import com.knowave.monomarket.domains.user.repository.SocialAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class SocialAccountService(
    private val socialAccountRepository: SocialAccountRepository,
) {
    @Transactional(readOnly = true)
    fun getSocialAccount(
        provider: String,
        providerUserId: String,
    ): SocialAccount? {
        return socialAccountRepository.findByProviderAndProviderUserId(
            provider = provider,
            providerUserId = providerUserId,
        )
    }

    @Transactional
    fun createSocialAccount(
        user: User,
        provider: String,
        providerUserId: String,
        email: String?,
    ): SocialAccount {
        return socialAccountRepository.save(
            SocialAccount(
                user = user,
                provider = provider,
                providerUserId = providerUserId,
                email = email,
            ),
        )
    }

    @Transactional
    fun deleteManySocialAccountByUser(userId: UUID) {
        socialAccountRepository.deleteAllByUserId(userId)
    }
}
