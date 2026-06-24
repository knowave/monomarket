package com.knowave.monomarket.domains.user.dto

import java.util.UUID

data class UserMeResponse(
    val id: UUID,
    val nickname: String,
)
