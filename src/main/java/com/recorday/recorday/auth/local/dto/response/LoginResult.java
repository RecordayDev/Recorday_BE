package com.recorday.recorday.auth.local.dto.response;

import com.recorday.recorday.auth.jwt.dto.AuthTokenCookies;
import com.recorday.recorday.user.enums.UserStatus;

public record LoginResult(
	AuthTokenCookies cookies,
	UserStatus userStatus
) {
}
