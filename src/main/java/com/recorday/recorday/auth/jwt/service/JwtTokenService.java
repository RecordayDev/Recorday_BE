package com.recorday.recorday.auth.jwt.service;

import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;

public interface JwtTokenService {

	String createAccessToken(Long userId);

	String createRefreshToken(Long userId);

	boolean validateToken(String token);

	Long getUserId(String token);

	AuthTokenResponse reissue(String refreshToken);

	void logout(String refreshToken);
}
