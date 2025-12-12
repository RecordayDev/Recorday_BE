package com.recorday.recorday.auth.oauth2.provider.naver.dto;

public record NaverUnlinkRequest(
	String clientId,
	String encryptUniqueId,
	String timestamp,
	String signature
) {
}
