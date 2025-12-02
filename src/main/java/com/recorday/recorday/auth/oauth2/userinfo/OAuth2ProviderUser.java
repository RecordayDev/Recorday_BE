package com.recorday.recorday.auth.oauth2.userinfo;

import java.util.Map;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

import com.recorday.recorday.auth.oauth2.enums.Provider;

import lombok.Getter;

@Getter
public abstract class OAuth2ProviderUser implements ProviderUser {

	private final Map<String, Object> attributes;
	private final ClientRegistration clientRegistration;

	public OAuth2ProviderUser(Map<String, Object> attributes, ClientRegistration clientRegistration) {
		this.attributes = attributes;
		this.clientRegistration = clientRegistration;
	}

	@Override
	public Provider getProvider() {
		return Provider.from(clientRegistration.getRegistrationId());
	}
}
