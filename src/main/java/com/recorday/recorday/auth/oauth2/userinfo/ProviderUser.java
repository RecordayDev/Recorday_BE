package com.recorday.recorday.auth.oauth2.userinfo;

import java.util.Map;

import com.recorday.recorday.auth.oauth2.enums.Provider;

public interface ProviderUser {

	String getProviderId();

	Provider getProvider();

	String getEmail();

	Map<String, Object> getAttributes();

	String getProfileImageUrl();

	String getNickname();
}
