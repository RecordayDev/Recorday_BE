package com.recorday.recorday.auth.local.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.recorday.recorday.auth.entity.CustomUserPrincipal;
import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.jwt.service.RefreshTokenService;
import com.recorday.recorday.auth.local.dto.request.LocalLoginRequest;
import com.recorday.recorday.auth.local.dto.response.LoginResult;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.enums.UserStatus;
import com.recorday.recorday.util.user.UserReader;

@ExtendWith(MockitoExtension.class)
class LocalSecurityLoginServiceImplTest {

	@InjectMocks
	private LocalSecurityLoginServiceImpl localLoginService;

	@Mock
	private UserReader userReader;
	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private JwtTokenService jwtTokenService;
	@Mock
	private RefreshTokenService refreshTokenService;

	@Mock
	private Authentication authentication;
	@Mock
	private CustomUserPrincipal principal;
	@Mock
	private User user;

	private final long ACCESS_EXPIRATION = 1800000L;
	private final long REFRESH_EXPIRATION = 604800000L;

	@BeforeEach
	void setUp() {
		// @Value 필드 주입
		ReflectionTestUtils.setField(localLoginService, "accessValidity", ACCESS_EXPIRATION);
		ReflectionTestUtils.setField(localLoginService, "refreshValidity", REFRESH_EXPIRATION);
	}

	@Test
	@DisplayName("로그인 성공 시 쿠키와 유저 상태를 포함한 결과를 반환한다")
	void login_success() {
		// Given
		LocalLoginRequest request = new LocalLoginRequest("test@email.com", "password");
		String publicId = "user-public-id";
		String accessToken = "access-token";
		String refreshToken = "refresh-token";

		// Authentication 모킹
		given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
			.willReturn(authentication);
		given(authentication.getPrincipal()).willReturn(principal);
		given(principal.getPublicId()).willReturn(publicId);

		// User 조회 모킹
		given(userReader.getUserByPublicId(publicId)).willReturn(user);
		given(user.getUserStatus()).willReturn(UserStatus.ACTIVE);

		// 토큰 생성 모킹
		given(jwtTokenService.createAccessToken(publicId)).willReturn(accessToken);
		given(jwtTokenService.createRefreshToken(publicId)).willReturn(refreshToken);

		// When
		LoginResult result = localLoginService.login(request);

		// Then
		// 1. Refresh Token 저장 호출 확인
		then(refreshTokenService).should().saveRefreshToken(publicId, refreshToken);

		// 2. 반환된 UserStatus 확인
		assertThat(result.userStatus()).isEqualTo(com.recorday.recorday.user.enums.UserStatus.ACTIVE);

		// 3. Access Token 쿠키 검증
		assertThat(result.cookies().accessTokenCookie().getValue()).isEqualTo(accessToken);
		assertThat(result.cookies().accessTokenCookie().getMaxAge()).isEqualTo(Duration.ofMillis(ACCESS_EXPIRATION));
		assertThat(result.cookies().accessTokenCookie().isHttpOnly()).isTrue();
		assertThat(result.cookies().accessTokenCookie().isSecure()).isTrue();

		// 4. Refresh Token 쿠키 검증
		assertThat(result.cookies().refreshTokenCookie().getValue()).isEqualTo(refreshToken);
		assertThat(result.cookies().refreshTokenCookie().getMaxAge()).isEqualTo(Duration.ofMillis(REFRESH_EXPIRATION));
		assertThat(result.cookies().refreshTokenCookie().isHttpOnly()).isTrue();
	}

	@Test
	@DisplayName("비밀번호 불일치 시 BadCredentialsException을 BusinessException으로 변환한다")
	void login_fail_badCredentials() {
		// Given
		LocalLoginRequest request = new LocalLoginRequest("test@email.com", "wrong-password");

		given(authenticationManager.authenticate(any()))
			.willThrow(new BadCredentialsException("Bad credentials"));

		// When & Then
		BusinessException ex = assertThrows(BusinessException.class,
			() -> localLoginService.login(request));

		assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);

		// 토큰 생성 로직이 실행되지 않았는지 확인
		then(jwtTokenService).shouldHaveNoInteractions();
	}
}