package com.recorday.recorday.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.BindException;

import com.recorday.recorday.exception.dto.FieldErrorResponse;
import com.recorday.recorday.util.response.Response;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 비즈니스 로직에서 의도적으로 던지는 예외 처리
	 * - Service/Domain 레이어에서 throw new BusinessException(...) 한 경우
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Response<Void>> handleBusinessException(BusinessException ex) {
		log.warn("[BusinessException] code={}, message={}", ex.getErrorCode().getCode(), ex.getMessage());

		Response<Void> response = Response.errorResponse(ex.getErrorCode());
		return response.toResponseEntity();
	}

	/**
	 * @Valid + @RequestBody 검증 실패 시 발생
	 * - DTO 필드 제약조건 위반 (Bean Validation)
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Response<List<FieldErrorResponse>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		log.warn("[MethodArgumentNotValidException] message={}", ex.getMessage());

		List<FieldErrorResponse> errors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> new FieldErrorResponse(
				error.getField(),
				error.getDefaultMessage(),
				error.getRejectedValue()
			))
			.toList();

		Response<List<FieldErrorResponse>> response =
			Response.from(GlobalErrorCode.VALIDATION_FAILED, errors);
		return response.toResponseEntity();
	}

	/**
	 * @Valid 없이, 바인딩(값 매핑) 단계에서 에러가 난 경우
	 * - 주로 @ModelAttribute, @RequestParam 바인딩 실패
	 */
	@ExceptionHandler(BindException.class)
	public ResponseEntity<Response<Void>> handleBindException(BindException ex) {
		log.warn("[BindException] message={}", ex.getMessage());

		Response<Void> response = Response.errorResponse(GlobalErrorCode.INVALID_INPUT_VALUE);
		return response.toResponseEntity();
	}

	/**
	 * @RequestParam, @PathVariable 타입 변환 실패 시
	 * - 예: Long id 파라미터에 "abc" 같은 값이 들어온 경우
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Response<Void>> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException ex
	) {
		log.warn("[MethodArgumentTypeMismatchException] message={}", ex.getMessage());

		Response<Void> response = Response.errorResponse(GlobalErrorCode.TYPE_MISMATCH);
		return response.toResponseEntity();
	}

	/**
	 * 필수 RequestParam 누락 시
	 * - 예: @RequestParam(required = true) 파라미터가 요청에 없는 경우
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Response<Void>> handleMissingServletRequestParameterException(
		MissingServletRequestParameterException ex
	) {
		log.warn("[MissingServletRequestParameterException] message={}", ex.getMessage());

		Response<Void> response = Response.errorResponse(GlobalErrorCode.MISSING_REQUEST_PARAMETER);
		return response.toResponseEntity();
	}

	/**
	 * 지원하지 않는 HTTP 메서드로 요청했을 때
	 * - 예: POST만 지원하는 API를 GET으로 호출한 경우
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<Response<Void>> handleHttpRequestMethodNotSupportedException(
		HttpRequestMethodNotSupportedException ex
	) {
		log.warn("[HttpRequestMethodNotSupportedException] message={}", ex.getMessage());

		Response<Void> response = Response.errorResponse(GlobalErrorCode.METHOD_NOT_ALLOWED);
		return response.toResponseEntity();
	}

	/**
	 * 지원하지 않는 Content-Type으로 요청한 경우
	 * - 예: application/json만 받는 API에 multipart/form-data 등으로 요청
	 */
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<Response<Void>> handleHttpMediaTypeNotSupportedException(
		HttpMediaTypeNotSupportedException ex
	) {
		log.warn("[HttpMediaTypeNotSupportedException] message={}", ex.getMessage());

		Response<Void> response = Response.errorResponse(GlobalErrorCode.UNSUPPORTED_MEDIA_TYPE);
		return response.toResponseEntity();
	}

	/**
	 * JSON 파싱 실패, RequestBody를 읽을 수 없는 경우
	 * - 예: 잘못된 JSON 문법, enum 매핑 실패, RequestBody 비어있음 등
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Response<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		log.warn("[HttpMessageNotReadableException] message={}", ex.getMessage());

		Response<Void> response = Response.errorResponse(GlobalErrorCode.JSON_PARSE_ERROR);
		return response.toResponseEntity();
	}

	/**
	 * PathVariable / RequestParam에 직접 Bean Validation을 사용했을 때 실패하는 경우
	 * - 예: @Min, @Max, @Size 등을 PathVariable, RequestParam에 붙였을 때
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Response<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
		log.warn("[ConstraintViolationException] message={}", ex.getMessage());

		Response<Void> response = Response.errorResponse(GlobalErrorCode.INVALID_INPUT_VALUE);
		return response.toResponseEntity();
	}

	/**
	 * 처리되지 않은 모든 예외에 대한 최종 fallback
	 * - 예상하지 못한 서버 내부 오류
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Response<Void>> handleException(Exception ex) {
		log.error("[Exception] message={}", ex.getMessage());

		Response<Void> response = Response.errorResponse(GlobalErrorCode.INTERNAL_SERVER_ERROR);
		return response.toResponseEntity();
	}
}
