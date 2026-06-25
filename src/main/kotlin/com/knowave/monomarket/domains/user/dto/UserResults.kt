package com.knowave.monomarket.domains.user.dto

import java.util.UUID

data class UserMeResult(
    val id: UUID,
    val nickname: String,
)
