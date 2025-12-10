package com.recorday.recorday.auth.oauth2.component;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.auth.oauth2.provider.kakao.adaptor.KakaoOidcUser;
import com.recorday.recorday.auth.oauth2.provider.naver.adaptor.NaverUser;
import com.recorday.recorday.auth.oauth2.userinfo.ProviderUser;

public final class ProviderUserFactory {

	public static ProviderUser of(Provider provider, OAuth2User oAuth2User, ClientRegistration clientRegistration) {
		return switch (provider) {
			// case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
			case KAKAO -> new KakaoOidcUser(oAuth2User, clientRegistration);
			case NAVER -> new NaverUser(oAuth2User, clientRegistration);
			default -> throw new IllegalArgumentException("지원하지 않는 provider: " + provider);
		};
	}
}
