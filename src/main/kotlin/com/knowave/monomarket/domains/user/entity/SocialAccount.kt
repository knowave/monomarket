package com.knowave.monomarket.domains.user.entity

import com.knowave.monomarket.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "social_accounts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_social_account_provider_user",
            columnNames = ["provider", "provider_user_id"],
        ),
        UniqueConstraint(
            name = "uk_social_account_user_provider",
            columnNames = ["user_id", "provider"],
        ),
    ],
)
class SocialAccount(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, length = 50)
    var provider: String,

    @Column(name = "provider_user_id", nullable = false, length = 255)
    var providerUserId: String,

    @Column(length = 255)
    var email: String? = null,
) : BaseEntity()
