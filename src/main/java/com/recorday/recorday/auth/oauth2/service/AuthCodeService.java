package com.recorday.recorday.auth.oauth2.service;

import com.recorday.recorday.auth.jwt.dto.AuthCodePayload;

public interface AuthCodeService {

	String saveAuthCode(String publicId, String provider);

	AuthCodePayload getAuthPayload(String code);

	void deleteAuthCode(String code);
}
