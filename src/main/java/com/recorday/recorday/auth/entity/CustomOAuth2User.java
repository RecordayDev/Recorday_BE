package com.recorday.recorday.auth.entity;

import java.util.Map;

import org.springframework.security.oauth2.core.user.OAuth2User;

import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.user.entity.User;

import lombok.Getter;

@Getter
public class CustomOAuth2User extends CustomUserPrincipal implements OAuth2User {

	private final Map<String, Object> attributes;
	private final String nameAttributeKey;
	private final Provider provider;

	public CustomOAuth2User(User user,
		Map<String, Object> attributes,
		String nameAttributeKey,
		Provider provider) {
		super(user);
		this.attributes = attributes;
		this.nameAttributeKey = nameAttributeKey;
		this.provider = provider;
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
