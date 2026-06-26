package com.knowave.monomarket.domains.user.repository

import com.knowave.monomarket.domains.user.entity.SocialAccount
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SocialAccountRepository : JpaRepository<SocialAccount, UUID> {
    fun findByProviderAndProviderUserId(provider: String, providerUserId: String): SocialAccount?

    fun deleteAllByUserId(userId: UUID)
}
