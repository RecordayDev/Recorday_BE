package com.recorday.recorday.auth.local.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

public record EmailAuthRequest(

	@Schema(description = "인증 코드를 받을 이메일 주소", example = "user@recorday.com")
	@Email
	String email
) {
}
