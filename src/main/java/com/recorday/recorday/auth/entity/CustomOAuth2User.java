package com.recorday.recorday.auth.entity;

import java.util.Map;

import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.user.entity.User;

import lombok.Getter;

@Getter
public class CustomOAuth2User extends CustomUserPrincipal implements OAuth2User, OidcUser {

	private final Map<String, Object> attributes;
	private final String nameAttributeKey;
	private final Provider provider;

	private final OidcIdToken idToken;
	private final OidcUserInfo userInfo;

	public CustomOAuth2User(User user,
		Map<String, Object> attributes,
		String nameAttributeKey,
		Provider provider) {
		this(user, attributes, nameAttributeKey, provider, null, null);
	}

	public CustomOAuth2User(User user,
		Map<String, Object> attributes,
		String nameAttributeKey,
		Provider provider,
		OidcIdToken idToken,
		OidcUserInfo userInfo) {
		super(user);
		this.attributes = attributes;
		this.nameAttributeKey = nameAttributeKey;
		this.provider = provider;
		this.idToken = idToken;
		this.userInfo = userInfo;
	}

	@Override
	public Map<String, Object> getClaims() {
		return this.idToken != null ? this.idToken.getClaims() : Map.of();
	}

	@Override
	public OidcUserInfo getUserInfo() {
		return this.userInfo;
	}

	@Override
	public OidcIdToken getIdToken() {
		return this.idToken;
	}

	@Override
	public String getName() {
		Object nameAttr = attributes.get(nameAttributeKey);
		return nameAttr != null ? nameAttr.toString() : getEmail();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
