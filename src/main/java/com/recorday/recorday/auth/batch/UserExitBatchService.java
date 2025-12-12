package com.recorday.recorday.auth.batch;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.service.UserExitService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserExitBatchService {

	private final UserExitService userExitService;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void exitInNewTransaction(Long userId) {
		userExitService.exit(userId);
	}
}
