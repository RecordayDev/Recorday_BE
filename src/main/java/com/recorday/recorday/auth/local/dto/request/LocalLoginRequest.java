package com.recorday.recorday.auth.local.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LocalLoginRequest(

	@Email(message = "유효한 이메일 형식이 아닙니다.")
	@NotBlank(message = "이메일은 필수 값입니다.")
	String email,

	@NotBlank(message = "비밀번호는 필수 값입니다.")
	String password
) {}

