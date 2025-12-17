package com.recorday.recorday.auth.local.service.mail;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.util.user.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailAuthService {

	private final MailAuthCodeService mailAuthCodeService;
	private final StringRedisTemplate stringRedisTemplate;

	private static final String KEY_PREFIX = "REGISTER:EMAIL:";
	private static final long EXPIRATION = 60 * 10L;

	@Transactional
	public void sendAuthCode(String email) {
		mailAuthCodeService.sendCode(email);
	}

	@Transactional
	public void verifyAuthCode(String email, String inputCode) {

		mailAuthCodeService.verifyCode(email, inputCode);

		String key = KEY_PREFIX + email;
		stringRedisTemplate.opsForValue().set(key, "VERIFIED", EXPIRATION, TimeUnit.SECONDS);
	}
}
