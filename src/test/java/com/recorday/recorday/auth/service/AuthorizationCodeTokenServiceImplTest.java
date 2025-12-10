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
import com.recorday.recorday.auth.jwt.service.RefreshTokenService;
import com.recorday.recorday.auth.oauth2.service.AuthCodeService;

@ExtendWith(MockitoExtension.class)
class AuthorizationCodeTokenServiceImplTest {

	@Mock
	private AuthCodeService authCodeService;

	@Mock
	private JwtTokenService jwtTokenService;

	@Mock // [추가됨] 누락된 Mock 추가
	private RefreshTokenService refreshTokenService;

	@InjectMocks
	private AuthorizationCodeTokenServiceImpl authorizationCodeTokenService;

	@Test
	@DisplayName("코드 검증 후 레디스에서 삭제 및 토큰 발급")
	void issueTokens() {
		//given
		String code = "AUTH_CODE_123";
		String publicId = "publicId";
		AuthCodePayload payload = new AuthCodePayload(publicId, "kakao");

		given(authCodeService.getAuthPayload(code)).willReturn(payload);
		given(jwtTokenService.createAccessToken(publicId)).willReturn("ACCESS_TOKEN");
		given(jwtTokenService.createRefreshToken(publicId)).willReturn("REFRESH_TOKEN");

		// [추가됨] void 메서드는 willDoNothing이 기본이지만, 명시적으로 적어주거나 생략 가능
		// willDoNothing().given(refreshTokenService).saveRefreshToken(anyLong(), anyString());

		//when
		TokenResponse tokenResponse = authorizationCodeTokenService.issueTokens(code);

		//then
		assertThat(tokenResponse.accessToken()).isEqualTo("ACCESS_TOKEN");
		assertThat(tokenResponse.refreshToken()).isEqualTo("REFRESH_TOKEN");

		then(authCodeService).should().getAuthPayload(code);
		then(authCodeService).should().deleteAuthCode(code);
		then(jwtTokenService).should().createAccessToken(publicId);
		then(jwtTokenService).should().createRefreshToken(publicId);

		// [추가됨] RefreshToken 저장 호출 검증
		then(refreshTokenService).should().saveRefreshToken(publicId, "REFRESH_TOKEN");
	}
}