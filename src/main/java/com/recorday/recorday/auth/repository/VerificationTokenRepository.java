package com.recorday.recorday.auth.repository;

public interface VerificationTokenRepository {

	void save(String email, String code, long ttlInSeconds);

	String getCode(String email);

	void remove(String email);
}
