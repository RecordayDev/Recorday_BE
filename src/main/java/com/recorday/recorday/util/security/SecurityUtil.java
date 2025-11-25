package com.recorday.recorday.util.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

	private final UserRepository userRepository;

	// public User getCurrentUser() {
	// 	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	//
	// 	if (authentication != null && authentication.getPrincipal() instanceof CustomUser customUser) {
	// 		String email = customUser.getEmail();
	//
	// 		return userRepository.findByEmail(email)
	// 			.orElseThrow(() -> BusinessException.builder()
	// 				.errorCode(AuthErrorCode.NOT_EXIST_USER)
	// 				.build());
	// 	}
	//
	// 	throw BusinessException.builder()
	// 		.errorCode(AuthErrorCode.NOT_EXIST_USER)
	// 		.build();
	// }

}
