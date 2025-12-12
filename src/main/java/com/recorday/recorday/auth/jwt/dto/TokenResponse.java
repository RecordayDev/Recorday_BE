package com.recorday.recorday.auth.jwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(

	@Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsIn...")
	String accessToken,

	@Schema(description = "JWT 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsIn...")
	String refreshToken
) {
}
