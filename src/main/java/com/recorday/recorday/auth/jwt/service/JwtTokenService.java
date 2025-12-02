package com.recorday.recorday.auth.jwt.service;

public interface JwtTokenService {

	String createAccessToken(Long userId);

	String createRefreshToken(Long userId);

	boolean validateToken(String token);

	Long getUserId(String token);

	public String getTokenType(String token);

	public long getRefreshTokenValidityMillis();
}
