package com.recorday.recorday.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum GlobalErrorCode implements ErrorCode{
	// 400
	BAD_REQUEST("GEN-001", HttpStatus.BAD_REQUEST, "Bad Request"),
	INVALID_INPUT_VALUE("GEN-002", HttpStatus.BAD_REQUEST, "Invalid Input Value"),
	VALIDATION_FAILED("GEN-003", HttpStatus.BAD_REQUEST, "Validation Failed"),
	MISSING_REQUEST_PARAMETER("GEN-004", HttpStatus.BAD_REQUEST, "Missing Request Parameter"),
	TYPE_MISMATCH("GEN-005", HttpStatus.BAD_REQUEST, "Type Mismatch"),
	JSON_PARSE_ERROR("GEN-006", HttpStatus.BAD_REQUEST, "Json Parse Error"),

	// 404
	NOT_FOUND("GEN-007", HttpStatus.NOT_FOUND, "Resource Not Found"),

	// 405
	METHOD_NOT_ALLOWED("GEN-008", HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed"),

	// 415
	UNSUPPORTED_MEDIA_TYPE("GEN-009", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type"),

	// 401/403
	UNAUTHORIZED("GEN-010", HttpStatus.UNAUTHORIZED, "Unauthorized"),
	FORBIDDEN("GEN-011", HttpStatus.FORBIDDEN, "Forbidden"),

	// 500
	INTERNAL_SERVER_ERROR("GEN-099", HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),
	DATABASE_ERROR("GEN-100", HttpStatus.INTERNAL_SERVER_ERROR, "Database Error"),
	IO_ERROR("GEN-101", HttpStatus.INTERNAL_SERVER_ERROR, "IO Error");
	;

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;

	GlobalErrorCode(String code, HttpStatus httpStatus, String message) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
