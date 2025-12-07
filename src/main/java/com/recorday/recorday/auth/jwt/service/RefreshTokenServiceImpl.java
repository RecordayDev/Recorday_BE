package com.recorday.recorday.auth.jwt.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;
import com.recorday.recorday.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

	private static final String KEY_PREFIX = "REFRESH_TOKEN:USER:";

	private final StringRedisTemplate stringRedisTemplate;
	private final JwtTokenService jwtTokenService;

	private String buildKey(String publicId) {
		return KEY_PREFIX + publicId;
	}

	@Override
	@Transactional
	public void saveRefreshToken(String publicId, String refreshToken) {
		String key = buildKey(publicId);
		Duration ttl = Duration.ofMillis(jwtTokenService.getRefreshTokenValidityMillis());

		stringRedisTemplate.opsForValue().set(key, refreshToken, ttl);
	}

	@Override
	@Transactional
	public AuthTokenResponse reissue(String refreshToken) {

		jwtTokenService.validateToken(refreshToken);

		String tokenType = jwtTokenService.getTokenType(refreshToken);
		if (!"REFRESH".equals(tokenType)) {
			throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
		}

		String publicId = jwtTokenService.getUserPublicId(refreshToken);

		String key = buildKey(publicId);
		String storedRefreshToken = stringRedisTemplate.opsForValue().get(key);

		if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
			throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
		}

		String newAccessToken = jwtTokenService.createAccessToken(publicId);
		String newRefreshToken = jwtTokenService.createRefreshToken(publicId);

		Duration ttl = Duration.ofMillis(jwtTokenService.getRefreshTokenValidityMillis());
		stringRedisTemplate
			.opsForValue()
			.set(key, newRefreshToken, ttl);

		return new AuthTokenResponse(newAccessToken, newRefreshToken);
	}

	@Override
	@Transactional
	public void logout(String publicId) {
		String key = buildKey(publicId);
		stringRedisTemplate.delete(key);
	}
}
