package com.recorday.recorday.auth.local.service;

public interface PasswordService {

	void sendPasswordResetMail(String email);

	void resetPassword(String token, String newPassword);

	void changePassword(String email, String oldPassword, String newPassword);
}
