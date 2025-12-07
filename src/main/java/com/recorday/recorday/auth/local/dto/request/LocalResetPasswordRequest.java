package com.recorday.recorday.auth.local.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 재설정(초기화) 요청 DTO")
public record LocalResetPasswordRequest(

	@Schema(description = "새로운 비밀번호", example = "newPassword123!")
	@NotBlank(message = "새로운 비밀번호는 필수 값입니다.")
	String newPassword
) {
}