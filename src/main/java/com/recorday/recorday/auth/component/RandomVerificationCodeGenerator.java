package com.recorday.recorday.auth.component;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class RandomVerificationCodeGenerator implements VerificationCodeGenerator {

	private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int CODE_LENGTH = 6;
	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	public String generate() {

		StringBuilder sb = new StringBuilder(CODE_LENGTH);

		for (int i = 0; i < CODE_LENGTH; i++) {
			int randomIndex = secureRandom.nextInt(ALPHANUMERIC.length());
			sb.append(ALPHANUMERIC.charAt(randomIndex));
		}

		return sb.toString();
	}
}
