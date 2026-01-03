package com.recorday.recorday.auth.local.dto.response;

import com.recorday.recorday.user.enums.UserStatus;

public record LoginResponse(
	UserStatus userStatus
) {
}
