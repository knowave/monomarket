package com.knowave.monomarket.domains.user.dto

data class UpdateNicknameCommand(
    val nickname: String,
)

data class DeleteMyAccountCommand(
    val reason: String? = null,
)
