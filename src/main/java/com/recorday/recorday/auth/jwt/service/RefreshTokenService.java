package com.recorday.recorday.auth.jwt.service;

import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;

public interface RefreshTokenService {

	void saveRefreshToken(Long userId, String refreshToken);

	AuthTokenResponse reissue(String refreshToken);

	void logout(Long userId);
}
