package com.recorday.recorday.user.exception;

import org.springframework.http.HttpStatus;

import com.recorday.recorday.exception.ErrorCode;

import lombok.Getter;

@Getter
public enum UserErrorCode implements ErrorCode {

	NOT_TARGET("USR-001", HttpStatus.BAD_REQUEST, "탈퇴 대상이 아닌 사용자입니다."),
	;

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;

	UserErrorCode(String code, HttpStatus httpStatus, String message) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
