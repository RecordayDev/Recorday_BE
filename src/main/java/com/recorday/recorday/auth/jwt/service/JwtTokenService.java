package com.recorday.recorday.auth.jwt.service;

public interface JwtTokenService {

	String createAccessToken(String publicId);

	String createRefreshToken(String publicId);

	void validateToken(String token);

	String getUserPublicId(String token);

	public String getTokenType(String token);

	public long getRefreshTokenValidityMillis();
}
