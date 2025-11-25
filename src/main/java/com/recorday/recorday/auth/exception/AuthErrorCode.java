package com.recorday.recorday.auth.exception;

import org.springframework.http.HttpStatus;

import com.recorday.recorday.exception.ErrorCode;

import lombok.Getter;

@Getter
public enum AuthErrorCode implements ErrorCode {
	INVALID_CREDENTIALS("AUTH-001", HttpStatus.UNAUTHORIZED, "Invalid Credentials"),
	NOT_EXIST_USER("AUTH-002", HttpStatus.NOT_FOUND, "NOT_EXIST_USER"),
	WRONG_PASSWORD("AUTH-003", HttpStatus.NOT_FOUND, "WRONG_PASSWORD")
	;

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;

	AuthErrorCode(String code, HttpStatus httpStatus, String message) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
