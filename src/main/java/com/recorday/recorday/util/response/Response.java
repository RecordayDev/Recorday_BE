package com.recorday.recorday.util.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.recorday.recorday.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

	private static final String OK_CODE = "GEN-000";

	private String code;
	private int status;
	private String message;
	private T data;

	public static Response<Void> ok() {
		Response<Void> response = new Response<>();
		response.code = OK_CODE;
		response.status = HttpStatus.OK.value();
		return response;
	}

	public static <T> Response<T> ok(T data) {
		Response<T> response = new Response<>();
		response.code = OK_CODE;
		response.status = HttpStatus.OK.value();
		response.data = data;
		return response;
	}

	public static <T> Response<T> errorResponse(ErrorCode errorCode) {
		Response<T> response = new Response<>();
		response.code = errorCode.getCode();
		response.status = errorCode.getHttpStatus().value();
		response.message = errorCode.getMessage();
		return response;
	}

	public ResponseEntity<Response<T>> toResponseEntity() {
		HttpStatus httpStatus = HttpStatus.valueOf(this.status);
		return new ResponseEntity<>(this, httpStatus);
	}

	public static <T> Response<T> from(ErrorCode errorCode, T data) {
		return Response.<T>builder()
			.code(errorCode.getCode())
			.status(errorCode.getHttpStatus().value())
			.message(errorCode.getMessage())
			.data(data)
			.build();
	}
}
