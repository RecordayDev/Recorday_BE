package com.recorday.recorday.auth.utils;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

	public ResponseCookie createTokenCookie(String name, String value, long maxAgeMillis) {
		return ResponseCookie.from(name, value)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(Duration.ofMillis(maxAgeMillis))
			.sameSite("Strict")
			.build();
	}
}
