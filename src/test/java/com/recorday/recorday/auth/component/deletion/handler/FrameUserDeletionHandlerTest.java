package com.recorday.recorday.auth.component.deletion.handler;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.recorday.recorday.frame.repository.FrameRepository;

@ExtendWith(MockitoExtension.class)
class FrameUserDeletionHandlerTest {

	@Mock
	private FrameRepository frameRepository;

	@InjectMocks
	private FrameUserDeletionHandler frameUserDeletionHandler;

	@Test
	@DisplayName("회원 탈퇴 시 해당 유저의 모든 프레임을 삭제해야 한다.")
	void handleUserDeletionTest() {
		//given
		Long userId = 1L;

		//when
		frameUserDeletionHandler.handleUserDeletion(userId);

		//then
		then(frameRepository).should(times(1)).deleteComponentsByUserId(userId);
		then(frameRepository).should(times(1)).deleteFramesByUserId(userId);

	}


}