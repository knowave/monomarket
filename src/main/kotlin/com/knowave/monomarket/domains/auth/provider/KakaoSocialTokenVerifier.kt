package com.knowave.monomarket.domains.auth.provider

import com.knowave.monomarket.common.enum.SocialProvider
import com.knowave.monomarket.domains.auth.dto.SocialUserInfo
import org.springframework.stereotype.Component

@Component
class KakaoSocialTokenVerifier : SocialTokenVerifier {
    override fun supports(provider: SocialProvider): Boolean {
        return provider == SocialProvider.KAKAO
    }

    override fun verify(token: String): SocialUserInfo {
        return MockSocialTokenParser.verify(SocialProvider.KAKAO, token)
    }
}
