package com.recorday.recorday.util.user;

import org.springframework.stereotype.Component;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserReader {

	private final UserRepository userRepository;

	public User getUserByPublicId(String publicId) {
		return userRepository.findByPublicId(publicId)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.NOT_EXIST_USER));
	}

	public User getUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.NOT_EXIST_USER));
	}

	public User getUserByProviderAndProviderId(Provider provider, String providerId) {
		return userRepository.findByProviderAndProviderId(provider, providerId)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.NOT_EXIST_USER));
	}

}
