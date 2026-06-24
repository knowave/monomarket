package com.knowave.monomarket.domains.auth.entity

import com.knowave.monomarket.common.entity.BaseEntity
import com.knowave.monomarket.domains.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "refresh_tokens",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_refresh_token_token",
            columnNames = ["token"],
        ),
        UniqueConstraint(
            name = "uk_refresh_token_user_device",
            columnNames = ["user_id", "device_id"],
        ),
    ],
)
class RefreshToken(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, unique = true, length = 1000)
    var token: String,

    @Column(name = "device_id", nullable = false, length = 255)
    val deviceId: String,

    @Column(name = "device_name", length = 255)
    var deviceName: String? = null,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime,

    @Column(name = "revoked_at")
    var revokedAt: LocalDateTime? = null,

    @Column(name = "last_used_at")
    var lastUsedAt: LocalDateTime? = null,
) : BaseEntity() {
    fun replaceToken(
        token: String,
        expiresAt: LocalDateTime,
        deviceName: String?,
    ) {
        this.token = token
        this.expiresAt = expiresAt
        this.deviceName = deviceName
        this.revokedAt = null
        this.lastUsedAt = null
    }

    fun markUsed(usedAt: LocalDateTime) {
        this.lastUsedAt = usedAt
    }
}
