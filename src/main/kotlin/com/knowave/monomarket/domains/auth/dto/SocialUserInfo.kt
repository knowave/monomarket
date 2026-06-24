package com.knowave.monomarket.domains.auth.dto

import com.knowave.monomarket.common.enum.SocialProvider

data class SocialUserInfo(
    val provider: SocialProvider,
    val providerUserId: String,
    val email: String?,
    val nickname: String?,
    val profileImageUrl: String?,
)
