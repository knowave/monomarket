package com.knowave.monomarket.domains.auth.repository

import com.knowave.monomarket.domains.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByToken(token: String): RefreshToken?

    fun findByUserIdAndDeviceId(
        userId: UUID,
        deviceId: String,
    ): RefreshToken?

    fun deleteByUserIdAndDeviceId(
        userId: UUID,
        deviceId: String,
    )

    fun deleteAllByUserId(userId: UUID)

    fun existsByToken(token: String): Boolean

    fun findAllByUserIdAndRevokedAtIsNull(userId: UUID): List<RefreshToken>
}
