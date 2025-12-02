package com.recorday.recorday.auth.oauth2.service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.jwt.dto.AuthCodePayload;
import com.recorday.recorday.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AuthCodeServiceImpl implements AuthCodeService {

	private static final String KEY_PREFIX = "AUTH_CODE:";
	private static final long TTL_SECONDS = Duration.ofMinutes(5).getSeconds();

	private final StringRedisTemplate stringRedisTemplate;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional
	public String saveAuthCode(Long userId, String provider) {
		String authCode = createAuthorizationCode();
		String key = KEY_PREFIX + authCode;

		AuthCodePayload payload = new AuthCodePayload(userId, provider);

		String value = objectMapper.writeValueAsString(payload);

		stringRedisTemplate.opsForValue().set(key, value, TTL_SECONDS, TimeUnit.SECONDS);

		return authCode;
	}

	@Override
	public AuthCodePayload getAuthPayload(String code) {
		String key = KEY_PREFIX + code;
		String value = stringRedisTemplate.opsForValue().get(key);

		if (value == null) {
			throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS, "Not exist AuthCode in Redis");
		}
		return objectMapper.readValue(value, AuthCodePayload.class);
	}

	@Override
	@Transactional
	public void deleteAuthCode(String code) {
		String key = KEY_PREFIX + code;
		stringRedisTemplate.delete(key);
	}

	private String createAuthorizationCode() {
		return UUID.randomUUID().toString();
	}
}
