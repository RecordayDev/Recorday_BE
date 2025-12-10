package com.recorday.recorday.auth.local.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "자체 회원가입 요청 DTO")
public record LocalRegisterRequest(

	@Schema(description = "사용자 이메일", example = "newuser@recorday.com")
	@Email(message = "유효한 이메일 형식이 아닙니다.")
	@NotBlank(message = "이메일은 필수 값입니다.")
	String email,

	@Schema(description = "비밀번호", example = "password123!")
	@NotBlank(message = "비밀번호는 필수 값입니다.")
	String password,

	@Schema(description = "사용자 닉네임", example = "레코데이")
	@NotBlank(message = "닉네임은 필수 값입니다.")
	String username
) {
}