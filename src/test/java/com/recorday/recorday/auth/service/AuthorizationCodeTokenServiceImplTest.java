package com.recorday.recorday.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.recorday.recorday.auth.jwt.dto.AuthCodePayload;
import com.recorday.recorday.auth.jwt.dto.TokenResponse;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.oauth2.service.AuthCodeService;

@ExtendWith(MockitoExtension.class)
class AuthorizationCodeTokenServiceImplTest {

	@Mock
	private AuthCodeService authCodeService;

	@Mock
	private JwtTokenService jwtTokenService;

	@InjectMocks
	private AuthorizationCodeTokenServiceImpl authorizationCodeTokenService;

	@Test
	@DisplayName("코드 검증 후 레디스에서 삭제 및 토큰 발급")
	void issueTokens() {
		//given
		String code = "AUTH_CODE_123";
		AuthCodePayload payload = new AuthCodePayload(1L, "kakao");

		given(authCodeService.getAuthPayload(code)).willReturn(payload);
		given(jwtTokenService.createAccessToken(1L)).willReturn("ACCESS_TOKEN");
		given(jwtTokenService.createRefreshToken(1L)).willReturn("REFRESH_TOKEN");

		//when
		TokenResponse tokenResponse = authorizationCodeTokenService.issueTokens(code);

		//then
		assertThat(tokenResponse.accessToken()).isEqualTo("ACCESS_TOKEN");
		assertThat(tokenResponse.refreshToken()).isEqualTo("REFRESH_TOKEN");

		then(authCodeService).should().getAuthPayload(code);
		then(authCodeService).should().deleteAuthCode(code);
		then(jwtTokenService).should().createAccessToken(1L);
		then(jwtTokenService).should().createRefreshToken(1L);

	}
}