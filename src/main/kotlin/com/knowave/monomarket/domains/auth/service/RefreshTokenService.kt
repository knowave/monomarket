package com.knowave.monomarket.domains.auth.service

import com.knowave.monomarket.domains.auth.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    @Transactional
    fun deleteManyRefreshTokenByUser(userId: UUID) {
        refreshTokenRepository.deleteAllByUserId(userId)
    }
}
