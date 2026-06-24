package com.knowave.monomarket.domains.auth.provider

import com.knowave.monomarket.common.enum.SocialProvider
import com.knowave.monomarket.domains.auth.dto.SocialUserInfo

interface SocialTokenVerifier {
    fun supports(provider: SocialProvider): Boolean

    fun verify(token: String): SocialUserInfo
}
