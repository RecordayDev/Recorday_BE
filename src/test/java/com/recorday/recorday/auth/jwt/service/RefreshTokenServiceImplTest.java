package com.recorday.recorday.auth.jwt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.recorday.recorday.auth.exception.AuthErrorCode;
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

	@Test
	@DisplayName("유효한 리프레시 토큰으로 재발급 성공")
	void reissue_success() {
		// given
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

		String refreshToken = "old-refresh-token";
		String publicId = "public-id";
		String key = "REFRESH_TOKEN:USER:" + publicId;
		String newAccessToken = "new-access-token";
		String newRefreshToken = "new-refresh-token";
		long refreshValidityMillis = 1000L;

		given(jwtTokenService.getTokenType(refreshToken)).willReturn("REFRESH");
		given(jwtTokenService.getUserPublicId(refreshToken)).willReturn(publicId);
		given(valueOperations.get(key)).willReturn(refreshToken);
		given(jwtTokenService.createAccessToken(publicId)).willReturn(newAccessToken);
		given(jwtTokenService.createRefreshToken(publicId)).willReturn(newRefreshToken);
		given(jwtTokenService.getRefreshTokenValidityMillis()).willReturn(refreshValidityMillis);

		//when
		AuthTokenResponse response = refreshTokenService.reissue(refreshToken);

		//then
		assertThat(response.accessToken()).isEqualTo(newAccessToken);
		assertThat(response.refreshToken()).isEqualTo(newRefreshToken);

		Duration ttl = Duration.ofMillis(refreshValidityMillis);
		verify(valueOperations).set(
			eq(key),
			eq(newRefreshToken),
			eq(ttl)
		);
	}

	@Test
	@DisplayName("유효하지 않은 리프레시 토큰이면 재발급에 실패한다 - 서명/만료 검증 단계")
	void reissue_invalidToken() {
		// given
		String refreshToken = "invalid-token";

		// when
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> refreshTokenService.reissue(refreshToken)
		);

		// then
		assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_TOKEN);
	}

	@Test
	@DisplayName("액세스 토큰을 리프레시 토큰처럼 보내면 재발급에 실패한다 - 토큰 타입 검증 단계")
	void reissue_wrongTokenType() {
		// given
		String refreshToken = "access-token-pretending-to-be-refresh";

		given(jwtTokenService.getTokenType(refreshToken)).willReturn("ACCESS");

		// when
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> refreshTokenService.reissue(refreshToken)
		);

		// then
		assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_TOKEN);
	}

	@Test
	@DisplayName("Redis 에 존재하지 않는 리프레시 토큰이면 재발급에 실패한다")
	void reissue_notFoundInRedis() {
		// given
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

		String refreshToken = "rt-not-in-redis";
		String publicId = "public-id";
		String key = "REFRESH_TOKEN:USER:" + publicId;

		given(jwtTokenService.getTokenType(refreshToken)).willReturn("REFRESH");
		given(jwtTokenService.getUserPublicId(refreshToken)).willReturn(publicId);

		given(valueOperations.get(key)).willReturn(null);

		// when
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> refreshTokenService.reissue(refreshToken)
		);

		// then
		assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_TOKEN);
	}

	@Test
	@DisplayName("로그아웃 시 Redis 에서 리프레시 토큰 키를 삭제한다")
	void logout_deletesRefreshTokenKey() {
		// given
		String publicId = "public-id";
		String key = "REFRESH_TOKEN:USER:" + publicId;

		// when
		refreshTokenService.logout(publicId);

		// then
		verify(stringRedisTemplate).delete(key);
	}

}