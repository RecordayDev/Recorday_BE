package com.recorday.recorday.auth.local.dto.response;

public record AuthTokenResponse(
	String accessToken,
	String refreshToken
) {}