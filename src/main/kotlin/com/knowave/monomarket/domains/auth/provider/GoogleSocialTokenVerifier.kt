package com.knowave.monomarket.domains.auth.provider

import com.knowave.monomarket.common.enum.SocialProvider
import com.knowave.monomarket.domains.auth.dto.SocialUserInfo
import org.springframework.stereotype.Component

@Component
class GoogleSocialTokenVerifier : SocialTokenVerifier {
    override fun supports(provider: SocialProvider): Boolean {
        return provider == SocialProvider.GOOGLE
    }

    override fun verify(token: String): SocialUserInfo {
        return MockSocialTokenParser.verify(SocialProvider.GOOGLE, token)
    }
}
