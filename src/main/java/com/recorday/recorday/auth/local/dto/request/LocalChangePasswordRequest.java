package com.recorday.recorday.auth.local.dto.request;

public record LocalChangePasswordRequest(
	String oldPassword,
	String newPassword
) {
}
