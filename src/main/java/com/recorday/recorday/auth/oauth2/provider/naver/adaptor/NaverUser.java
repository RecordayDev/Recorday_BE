package com.recorday.recorday.auth.oauth2.provider.naver.adaptor;

import java.util.Map;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.recorday.recorday.auth.oauth2.userinfo.OAuth2ProviderUser;

public class NaverUser extends OAuth2ProviderUser {

	private final Map<String, Object> naverProfile;

	public NaverUser(OAuth2User oAuth2User, ClientRegistration clientRegistration) {
		super(oAuth2User.getAttributes(), clientRegistration);
		this.naverProfile = (Map<String, Object>)getAttributes().get("response");
	}

	@Override
	public String getProviderId() {
		return (String)naverProfile.get("id");
	}

	@Override
	public String getEmail() {
		return (String)naverProfile.get("email");
	}

	@Override
	public String getProfileImageUrl() {
		return (String)naverProfile.get("profile_image");
	}

	@Override
	public String getNickname() {
		return (String)naverProfile.get("nickname");
	}
}
