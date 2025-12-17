package com.recorday.recorday.auth.local.service;

import com.recorday.recorday.auth.local.dto.response.EmailAuthVerifyResponse;

public interface PasswordService {

	EmailAuthVerifyResponse verifyAuthCode(String email, String inputCode);

	void resetPassword(String email, String newPassword);

	void verifyOldPassword(Long userId, String oldPassword);

	void changePassword(Long userId, String password, String oldPassword, String newPassword);
}
