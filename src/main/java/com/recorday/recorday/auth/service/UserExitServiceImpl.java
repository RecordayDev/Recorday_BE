package com.recorday.recorday.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.oauth2.service.OAuth2UnlinkService;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserExitServiceImpl implements UserExitService{

	private final UserRepository userRepository;
	private final List<UserDeletionHandler> handlers;
	private final List<OAuth2UnlinkService> unlinkServices;

	@Override
	@Transactional
	public void exit(Long userId) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.NOT_EXIST_USER));

		user.delete();

		handlers.forEach(handler -> handler.handleUserDeletion(userId));

		for (OAuth2UnlinkService unlinkService : unlinkServices) {
			if (unlinkService.supports(user.getProvider())) {
				unlinkService.unlink(user);
				break;
			}
		}
	}
}
