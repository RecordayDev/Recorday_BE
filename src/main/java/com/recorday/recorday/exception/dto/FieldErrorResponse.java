package com.recorday.recorday.exception.dto;

public record FieldErrorResponse (
	String field,
	String message,
	Object rejectedValue
) {}
