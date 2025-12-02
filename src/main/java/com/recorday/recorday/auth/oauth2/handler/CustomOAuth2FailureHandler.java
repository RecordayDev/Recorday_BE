package com.recorday.recorday.auth.oauth2.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.recorday.recorday.auth.exception.AuthErrorCode;
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
public class CustomOAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Value("${redirect-url.frontend}")
	private String REDIRECT_URL;

	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException, ServletException {

		log.warn("OAuth2 실패 핸들러 도착 - type: {}, message: {}",
			exception.getClass().getName(),
			exception.getMessage(),
			exception);

		Throwable cause = exception.getCause();
		if (cause != null) {
			log.warn("OAuth2 실패 원인(cause) - type: {}, message: {}",
				cause.getClass().getName(),
				cause.getMessage(),
				cause);
		}

		AuthErrorCode errorCode = resolveErrorCode(exception);

		Response<Void> body = Response.errorResponse(errorCode);

		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType("application/json;charset=UTF-8");

		// objectMapper.writeValue(response.getWriter(), body);
		getRedirectStrategy().sendRedirect(request, response,REDIRECT_URL + "/failure");
	}

	private AuthErrorCode resolveErrorCode(AuthenticationException exception) {

		if (exception instanceof BadCredentialsException) {
			return AuthErrorCode.INVALID_CREDENTIALS;
		}
		if (exception instanceof UsernameNotFoundException) {
			return AuthErrorCode.INVALID_CREDENTIALS;
		}

		return AuthErrorCode.INVALID_CREDENTIALS;
	}
}
