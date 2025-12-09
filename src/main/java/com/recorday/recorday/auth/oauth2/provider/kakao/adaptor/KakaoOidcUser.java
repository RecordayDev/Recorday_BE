package com.recorday.recorday.auth.oauth2.provider.kakao.adaptor;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.recorday.recorday.auth.oauth2.userinfo.OAuth2ProviderUser;

public class KakaoOidcUser extends OAuth2ProviderUser {

	public KakaoOidcUser(OAuth2User oAuth2User,
		ClientRegistration clientRegistration) {
		super(oAuth2User.getAttributes(), clientRegistration);
	}

	@Override
	public String getProviderId() {
		return (String)getAttributes().get("sub");
	}

	@Override
	public String getEmail() {
		return (String)getAttributes().get("email");
	}

	@Override
	public String getProfileImageUrl() {
		return (String)getAttributes().get("picture");
	}

	@Override
	public String getNickname() {
		return (String)getAttributes().get("nickname");
	}
}
