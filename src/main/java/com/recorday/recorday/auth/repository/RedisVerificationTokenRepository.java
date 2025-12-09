package com.recorday.recorday.auth.repository;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisVerificationTokenRepository implements VerificationTokenRepository {

	private final StringRedisTemplate redisTemplate;

	private static final String KEY_PREFIX = "auth:email:";

	@Override
	public void save(String email, String code, long ttlInSeconds) {

		String key = KEY_PREFIX + email;

		redisTemplate.opsForValue()
			.set(key, code, Duration.ofSeconds(ttlInSeconds));
	}

	@Override
	public String getCode(String email) {

		String key = KEY_PREFIX + email;
		return redisTemplate.opsForValue().get(key);
	}

	@Override
	public void remove(String email) {

		String key = KEY_PREFIX + email;

		redisTemplate.delete(key);
	}
}

