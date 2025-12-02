package com.recorday.recorday.auth.jwt.dto;

public record AuthCodePayload(
	Long userId,
	String provider
) {
}
