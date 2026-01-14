package com.recorday.recorday.auth.jwt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.exception.CustomAuthenticationException;
import com.recorday.recorday.auth.jwt.dto.AuthTokenCookies;
import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;
import com.recorday.recorday.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Mock
	private JwtTokenService jwtTokenService;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@InjectMocks
	private RefreshTokenServiceImpl refreshTokenService;

	// 테스트용 만료 시간 설정 (밀리초)
	private final long TEST_ACCESS_EXPIRATION = 1800000L; // 30분
	private final long TEST_REFRESH_EXPIRATION = 604800000L; // 7일

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(refreshTokenService, "accessValidity", TEST_ACCESS_EXPIRATION);
		ReflectionTestUtils.setField(refreshTokenService, "refreshValidity", TEST_REFRESH_EXPIRATION);
	}

	@Test
	@DisplayName("유효한 리프레시 토큰으로 재발급 시, 쿠키 설정(MaxAge 등)이 올바르게 적용되어 반환된다")
	void reissue_success() {
		// given
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

		String oldRefreshToken = "old-refresh-token";
		String publicId = "user-public-id-123";
		String key = "REFRESH_TOKEN:USER:" + publicId;

		String newAccessToken = "new-access-token";
		String newRefreshToken = "new-refresh-token";

		// 1. JWT 서비스 동작 정의
		given(jwtTokenService.getTokenType(oldRefreshToken)).willReturn("REFRESH");
		given(jwtTokenService.getUserPublicId(oldRefreshToken)).willReturn(publicId);

		// Redis TTL 설정을 위해 호출됨 (서비스 코드 로직 확인)
		given(jwtTokenService.getRefreshTokenValidityMillis()).willReturn(TEST_REFRESH_EXPIRATION);

		// 2. Redis 조회 결과 정의
		given(valueOperations.get(key)).willReturn(oldRefreshToken);

		// 3. 새 토큰 생성 정의
		given(jwtTokenService.createAccessToken(publicId)).willReturn(newAccessToken);
		given(jwtTokenService.createRefreshToken(publicId)).willReturn(newRefreshToken);

		// when
		AuthTokenCookies response = refreshTokenService.reissue(oldRefreshToken);

		// then
		// 1. Access Token 쿠키 검증
		ResponseCookie accessCookie = response.accessTokenCookie();
		assertThat(accessCookie.getValue()).isEqualTo(newAccessToken);
		assertThat(accessCookie.getMaxAge()).isEqualTo(Duration.ofMillis(TEST_ACCESS_EXPIRATION)); // @Value 주입값 확인
		assertThat(accessCookie.isHttpOnly()).isTrue();
		assertThat(accessCookie.isSecure()).isTrue();
		assertThat(accessCookie.getSameSite()).isEqualTo("Strict");
		assertThat(accessCookie.getPath()).isEqualTo("/");

		// 2. Refresh Token 쿠키 검증
		ResponseCookie refreshCookie = response.refreshTokenCookie();
		assertThat(refreshCookie.getValue()).isEqualTo(newRefreshToken);
		assertThat(refreshCookie.getMaxAge()).isEqualTo(Duration.ofMillis(TEST_REFRESH_EXPIRATION)); // @Value 주입값 확인
		assertThat(refreshCookie.isHttpOnly()).isTrue();
		assertThat(refreshCookie.isSecure()).isTrue();
		assertThat(refreshCookie.getSameSite()).isEqualTo("Strict");
		assertThat(refreshCookie.getPath()).isEqualTo("/");

		// 3. Redis 업데이트 검증 (TTL이 제대로 넘어갔는지 확인)
		verify(valueOperations).set(
			eq(key),
			eq(newRefreshToken),
			eq(Duration.ofMillis(TEST_REFRESH_EXPIRATION))
		);
	}

	@Test
	@DisplayName("토큰 자체 검증(validateToken) 실패 시 예외가 전파된다")
	void reissue_validationFailed() {
		// given
		String invalidToken = "invalid-token";

		// validateToken 메서드가 예외를 던지도록 설정 (void 메서드 mock)
		willThrow(new CustomAuthenticationException(AuthErrorCode.INVALID_TOKEN))
			.given(jwtTokenService).validateToken(invalidToken);

		// when & then
		assertThrows(CustomAuthenticationException.class,
			() -> refreshTokenService.reissue(invalidToken));
	}

	@Test
	@DisplayName("토큰 타입이 REFRESH가 아니면 BusinessException(INVALID_TOKEN)이 발생한다")
	void reissue_wrongTokenType() {
		// given
		String accessToken = "access-token"; // 엑세스 토큰을 넣었다고 가정
		given(jwtTokenService.getTokenType(accessToken)).willReturn("ACCESS");

		// when & then
		BusinessException ex = assertThrows(BusinessException.class,
			() -> refreshTokenService.reissue(accessToken));

		assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_TOKEN);
	}

	@Test
	@DisplayName("Redis에 저장된 토큰이 없거나 불일치하면 BusinessException(INVALID_TOKEN)이 발생한다")
	void reissue_redisMismatch() {
		// given
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

		String requestToken = "token-from-user";
		String publicId = "user-public-id";
		String key = "REFRESH_TOKEN:USER:" + publicId;

		given(jwtTokenService.getTokenType(requestToken)).willReturn("REFRESH");
		given(jwtTokenService.getUserPublicId(requestToken)).willReturn(publicId);

		// Redis에서는 null 반환 (만료되었거나 로그아웃됨)
		given(valueOperations.get(key)).willReturn(null);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class,
			() -> refreshTokenService.reissue(requestToken));

		assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_TOKEN);
	}

	@Test
	@DisplayName("로그아웃 시 Redis 키를 삭제한다")
	void logout_success() {
		// given
		String publicId = "user-public-id";
		String key = "REFRESH_TOKEN:USER:" + publicId;

		// when
		refreshTokenService.logout(publicId);

		// then
		verify(stringRedisTemplate).delete(key);
	}
}