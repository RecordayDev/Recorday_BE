package com.recorday.recorday.auth.local.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 변경 요청 DTO")
public record LocalChangePasswordRequest(

	@Schema(description = "기존 비밀번호", example = "oldPassword123!")
	@NotBlank(message = "기존 비밀번호는 필수 값입니다.")
	String oldPassword,

	@Schema(description = "변경할 새로운 비밀번호", example = "newPassword123!")
	@NotBlank(message = "새로운 비밀번호는 필수 값입니다.")
	String newPassword
) {
}