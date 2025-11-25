package com.recorday.recorday.auth.local.dto.response;

public record LocalLoginResponse(
	String accessToken,
	String refreshToken
) {}