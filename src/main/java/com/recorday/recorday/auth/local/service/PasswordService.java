package com.recorday.recorday.auth.local.service;

// 일단은 단순하게 바로 변경하게 만들고
// 메일로 바꿀거면 mailsender 사용
public interface PasswordService {

	// void sendPasswordResetMail(String email);

	void resetPassword(Long userId, String token, String newPassword);

	void changePassword(Long userId, String password, String oldPassword, String newPassword);
}
