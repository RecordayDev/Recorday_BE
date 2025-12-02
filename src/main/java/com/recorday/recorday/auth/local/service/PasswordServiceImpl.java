package com.recorday.recorday.auth.local.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService{

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void resetPassword(Long userId, String token, String newPassword) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.NOT_EXIST_USER));

		user.changePassword(newPassword);
	}

	@Override
	@Transactional
	public void changePassword(Long userId, String password, String oldPassword, String newPassword) {

		if (!passwordEncoder.matches(password, oldPassword)) {
			throw new BusinessException(AuthErrorCode.WRONG_PASSWORD);
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.NOT_EXIST_USER));

		user.changePassword(newPassword);
	}
}
