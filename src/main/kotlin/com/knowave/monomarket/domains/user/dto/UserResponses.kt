package com.knowave.monomarket.domains.user.dto

import java.time.LocalDateTime
import java.util.UUID

data class GetUserProfileResponse(
    val id: UUID,
    val nickname: String,
    val profileImageUrl: String?,
    val createdAt: LocalDateTime,
)
