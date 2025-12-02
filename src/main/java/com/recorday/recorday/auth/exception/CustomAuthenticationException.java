package com.recorday.recorday.auth.exception;

import org.springframework.security.core.AuthenticationException;

import lombok.Getter;

@Getter
public class CustomAuthenticationException extends AuthenticationException {

	private final AuthErrorCode errorCode;

	public CustomAuthenticationException(AuthErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
