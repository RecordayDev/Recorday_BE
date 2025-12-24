package com.recorday.recorday.auth.exception;

import org.springframework.http.HttpStatus;

import com.recorday.recorday.exception.ErrorCode;

import lombok.Getter;

@Getter
public enum AuthErrorCode implements ErrorCode {
	AUTHENTICATION_FAILED("AUTH-000", HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED"),
	INVALID_CREDENTIALS("AUTH-001", HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS"),
	NOT_EXIST_USER("AUTH-002", HttpStatus.NOT_FOUND, "NOT_EXIST_USER"),
	WRONG_PASSWORD("AUTH-003", HttpStatus.BAD_REQUEST, "WRONG_PASSWORD"),
	EMAIL_DUPLICATED("AUTH-004", HttpStatus.BAD_REQUEST, "EMAIL_DUPLICATED"),
	DELETED_REQUEST_USER("AUTH-005", HttpStatus.BAD_REQUEST, "DELETED_REQUEST_USER"),
	DELETED_USER("AUTH-006", HttpStatus.BAD_REQUEST, "DELETED_USER"),
	OAUTH2_PROVIDER_UNLINK_ERROR("AUTH-007", HttpStatus.INTERNAL_SERVER_ERROR, "OAUTH2_PROVIDER_UNLINK_ERROR"),
	INVALID_TOKEN("AUTH-008", HttpStatus.UNAUTHORIZED, "INVALID_ACCESS_TOKEN"),
	EXPIRED_TOKEN("AUTH-009", HttpStatus.UNAUTHORIZED, "EXPIRED_ACCESS_TOKEN"),
	EMAIL_SEND_FAILED("AUTH-010", HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAILED"),
	EMAIL_AUTH_FAILED("AUTH-011", HttpStatus.BAD_REQUEST, "EMAIL_AUTH_FAILED"),
	REGISTER_EMAIL_ERROR("AUTH-012", HttpStatus.BAD_REQUEST, "REGISTER_EMAIL_ERROR"),
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
