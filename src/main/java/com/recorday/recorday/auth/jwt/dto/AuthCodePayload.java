package com.recorday.recorday.auth.jwt.dto;

public record AuthCodePayload(
	String publicId,
	String provider
) {
}
