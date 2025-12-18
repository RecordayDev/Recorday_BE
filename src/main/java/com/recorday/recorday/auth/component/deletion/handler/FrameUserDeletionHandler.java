package com.recorday.recorday.auth.component.deletion.handler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.service.UserDeletionHandler;
import com.recorday.recorday.frame.repository.FrameRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FrameUserDeletionHandler implements UserDeletionHandler {

	private final FrameRepository frameRepository;

	@Override
	@Transactional
	public void handleUserDeletion(Long userId) {
		frameRepository.deleteComponentsByUserId(userId);
		frameRepository.deleteFramesByUserId(userId);
	}
}
