package com.recorday.recorday.exception;

import lombok.Builder;
import lombok.Getter;

public class BusinessException extends RuntimeException {

	@Getter
	private final ErrorCode errorCode;

	@Builder
	public BusinessException(ErrorCode errorCode, String customMessage) {
		super(customMessage != null ? customMessage : errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
