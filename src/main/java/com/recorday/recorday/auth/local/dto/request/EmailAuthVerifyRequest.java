package com.recorday.recorday.auth.local.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailAuthVerifyRequest(

	@Schema(description = "인증 코드를 받은 이메일 주소", example = "user@recorday.com")
	@Email
	String email,

	@Schema(description = "이메일로 전송된 6자리 인증 코드", example = "1A2B3C")
	@NotBlank
	String code
) {
}
