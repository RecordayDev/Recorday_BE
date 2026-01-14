package com.recorday.recorday.auth.jwt.service;

import com.recorday.recorday.auth.jwt.dto.AuthTokenCookies;
import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;

public interface RefreshTokenService {

	void saveRefreshToken(String publicId, String refreshToken);

	AuthTokenCookies reissue(String refreshToken);

	void logout(String publicId);
}
