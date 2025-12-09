package com.recorday.recorday.auth.local.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.jwt.dto.TokenResponse;
import com.recorday.recorday.auth.local.service.mail.MailAuthCodeService;
import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.util.user.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

	private final UserReader userReader;
	private final PasswordEncoder passwordEncoder;
	private final MailAuthCodeService mailAuthCodeService;

	@Override
	@Transactional
	public void resetPassword(String email, String newPassword) {

		User user = userReader.getUserByEmailAndProvider(email, Provider.RECORDAY);

		user.changePassword(newPassword);
	}

	@Override
	@Transactional
	public void verifyAuthCode(String email, String inputCode) {
		mailAuthCodeService.verifyCode(email, inputCode);
	}

	@Override
	@Transactional
	public void changePassword(Long userId, String password, String oldPassword, String newPassword) {

		if (!passwordEncoder.matches(password, oldPassword)) {
			throw new BusinessException(AuthErrorCode.WRONG_PASSWORD);
		}

		User user = userReader.getUserById(userId);

		user.changePassword(newPassword);
	}
}
