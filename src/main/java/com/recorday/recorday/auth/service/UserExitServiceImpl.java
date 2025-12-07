package com.recorday.recorday.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.oauth2.service.OAuth2UnlinkService;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.util.user.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserExitServiceImpl implements UserExitService {

	private final UserReader userReader;
	private final List<UserDeletionHandler> handlers;
	private final List<OAuth2UnlinkService> unlinkServices;

	@Override
	@Transactional
	public void requestExit(Long userId) {

		User user = userReader.getUserById(userId);

		user.deleteRequested();
	}

	@Override
	@Transactional
	public void exit(Long userId) {

		User user = userReader.getUserById(userId);

		handlers.forEach(handler -> handler.handleUserDeletion(userId));

		for (OAuth2UnlinkService unlinkService : unlinkServices) {
			if (unlinkService.supports(user.getProvider())) {
				unlinkService.unlink(user);
				break;
			}
		}

		user.delete();
	}
}
