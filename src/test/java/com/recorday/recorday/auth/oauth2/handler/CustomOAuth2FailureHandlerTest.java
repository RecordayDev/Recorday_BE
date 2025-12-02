package com.recorday.recorday.auth.oauth2.handler;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.util.response.Response;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2FailureHandlerTest {

	private ObjectMapper objectMapper = new ObjectMapper();
	private CustomOAuth2FailureHandler failureHandler;

	@Test
	@DisplayName("OAuth2 인증 실패 시 리다이렉트")
	void onAuthenticationFailure() throws Exception{
		//given
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		AuthenticationException exception = new BadCredentialsException("bad credentials");

		failureHandler = new CustomOAuth2FailureHandler(objectMapper);
		ReflectionTestUtils.setField(failureHandler, "REDIRECT_URL", "http://localhost:5173");

		//when
		failureHandler.onAuthenticationFailure(request, response, exception);

		//then
		assertThat(response.getStatus()).isEqualTo(302);

		assertThat(response.getRedirectedUrl())
			.isEqualTo("http://localhost:5173/failure");

		assertThat(response.getContentAsString()).isEmpty();

	}

}