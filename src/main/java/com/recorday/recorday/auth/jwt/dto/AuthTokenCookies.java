package com.recorday.recorday.auth.jwt.dto;

import org.springframework.http.ResponseCookie;

public record AuthTokenCookies(
	ResponseCookie accessTokenCookie,
	ResponseCookie refreshTokenCookie
) {
}
