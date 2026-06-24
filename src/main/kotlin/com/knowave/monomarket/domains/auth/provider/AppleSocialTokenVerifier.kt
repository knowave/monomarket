package com.knowave.monomarket.domains.auth.provider

import com.knowave.monomarket.common.enum.SocialProvider
import com.knowave.monomarket.domains.auth.dto.SocialUserInfo
import org.springframework.stereotype.Component

@Component
class AppleSocialTokenVerifier : SocialTokenVerifier {
    override fun supports(provider: SocialProvider): Boolean {
        return provider == SocialProvider.APPLE
    }

    override fun verify(token: String): SocialUserInfo {
        return MockSocialTokenParser.verify(SocialProvider.APPLE, token)
    }
}
