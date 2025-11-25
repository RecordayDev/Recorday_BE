package com.recorday.recorday.auth.jwt.dto;

public record TokenResponse(
	String accessToken,
	String refreshToken
) {
}
