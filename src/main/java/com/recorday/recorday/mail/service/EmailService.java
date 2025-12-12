package com.recorday.recorday.mail.service;

public interface EmailService {

	void sendVerificationCode(String email, String subject, String code);
}
