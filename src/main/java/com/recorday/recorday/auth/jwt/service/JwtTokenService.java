package com.recorday.recorday.auth.jwt.service;

import com.recorday.recorday.auth.local.dto.response.LocalLoginResponse;

public interface JwtTokenService {

	String createAccessToken(Long userId);

	String createRefreshToken(Long userId);

	boolean validateToken(String token);

	LocalLoginResponse reissue(String refreshToken);

	void logout(String refreshToken);
}
