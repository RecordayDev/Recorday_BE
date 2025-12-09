package com.recorday.recorday.auth.local.service;

public interface PasswordService {

	void verifyAuthCode(String email, String inputCode);

	void resetPassword(String email, String newPassword);

	void changePassword(Long userId, String password, String oldPassword, String newPassword);
}
