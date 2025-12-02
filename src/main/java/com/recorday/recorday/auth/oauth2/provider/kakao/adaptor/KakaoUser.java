package com.recorday.recorday.auth.oauth2.provider.kakao.adaptor;

import java.util.Map;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.recorday.recorday.auth.oauth2.userinfo.OAuth2ProviderUser;

public class KakaoUser extends OAuth2ProviderUser {

	private final Map<String, Object> kakaoAccount;
	private final Map<String, Object> kakaoProfile;

	public KakaoUser(OAuth2User oAuth2User, ClientRegistration clientRegistration) {
		super(oAuth2User.getAttributes(), clientRegistration);

		Object account = getAttributes().get("kakao_account");
		this.kakaoAccount = account instanceof Map<?, ?> map ? (Map<String, Object>)map : Map.of();

		Object profile = kakaoAccount.get("profile");
		this.kakaoProfile = profile instanceof Map<?, ?> map ? (Map<String, Object>)map : Map.of();
	}

	@Override
	public String getEmail() {
		return (String)kakaoAccount.get("email");
	}

	@Override
	public String getProviderId() {
		return "" + getAttributes().get("id");
	}

	@Override
	public String getProfileImageUrl() {
		return (String)kakaoProfile.get("profile_image_url");
	}

	@Override
	public String getNickname() {
		return (String)kakaoProfile.get("nickname");
	}
}
