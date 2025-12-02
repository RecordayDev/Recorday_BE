package com.recorday.recorday.auth.component;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.exception.CustomAuthenticationException;
import com.recorday.recorday.exception.ErrorCode;
import com.recorday.recorday.util.response.Response;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException, ServletException {

		log.warn("[AuthEntryPoint] 인증 실패 - type: {}, message: {}",
			authException.getClass().getName(), authException.getMessage());

		AuthErrorCode errorCode = resolveErrorCode(authException);

		Response<Void> body = Response.errorResponse(errorCode);

		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType("application/json;charset=UTF-8");
		objectMapper.writeValue(response.getWriter(), body);
	}

	private AuthErrorCode resolveErrorCode(AuthenticationException ex) {

		if (ex instanceof CustomAuthenticationException customEx) {
			return customEx.getErrorCode();
		}

		return AuthErrorCode.INVALID_CREDENTIALS;
	}
}
