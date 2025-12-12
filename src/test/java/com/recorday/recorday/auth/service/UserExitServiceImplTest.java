package com.recorday.recorday.auth.service;

import com.recorday.recorday.auth.jwt.service.RefreshTokenService;
import com.recorday.recorday.auth.oauth2.service.OAuth2UnlinkService;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.enums.UserStatus;
import com.recorday.recorday.user.exception.UserErrorCode;
import com.recorday.recorday.util.user.UserReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserExitServiceImplTest {

	private UserExitServiceImpl userExitService;

	@Mock
	private UserReader userReader;

	@Mock
	private UserDeletionHandler userDeletionHandler;

	@Mock
	private RefreshTokenService refreshTokenService;

	@Mock
	private OAuth2UnlinkService oAuth2UnlinkService;

	private List<UserDeletionHandler> handlers;
	private List<OAuth2UnlinkService> unlinkServices;

	@BeforeEach
	void setUp() {
		handlers = new ArrayList<>();
		unlinkServices = new ArrayList<>();

		userExitService = new UserExitServiceImpl(userReader, refreshTokenService, handlers, unlinkServices);
	}

	@Test
	@DisplayName("탈퇴 요청 시 회원의 상태가 DELETED_REQUESTED로 변경된다")
	void requestExit_Success() {
		//given
		Long userId = 1L;
		User user = new User();
		given(userReader.getUserById(userId)).willReturn(user);

		//when
		userExitService.requestExit(userId);

		//then
		assertThat(user.getUserStatus()).isEqualTo(UserStatus.DELETED_REQUESTED);
		assertThat(user.getDeleteRequestedAt()).isNotNull();
	}

	@Test
	@DisplayName("완전 삭제(exit) 시 상태가 DELETED_REQUESTED가 아니면 예외가 발생한다")
	void exit_Fail_WhenNotRequested() {
		//given
		Long userId = 1L;
		User user = new User();

		given(userReader.getUserById(userId)).willReturn(user);

		//when & then
		assertThatThrownBy(() -> userExitService.exit(userId))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(UserErrorCode.NOT_TARGET);
	}

	@Test
	@DisplayName("완전 삭제(exit) 성공 시 핸들러와 언링크가 실행되고 상태가 DELETED로 변경된다")
	void exit_Success() {
		//given
		Long userId = 1L;
		User user = new User();
		user.deleteRequested();

		handlers.add(userDeletionHandler);
		unlinkServices.add(oAuth2UnlinkService);

		given(userReader.getUserById(userId)).willReturn(user);
		given(oAuth2UnlinkService.supports(any())).willReturn(true);

		//when
		userExitService.exit(userId);

		//then
		then(userDeletionHandler).should(times(1)).handleUserDeletion(userId);
		then(oAuth2UnlinkService).should(times(1)).unlink(user);

		assertThat(user.getUserStatus()).isEqualTo(UserStatus.DELETED);
		assertThat(user.getUsername()).isEqualTo("탈퇴한 사용자");
	}

	@Test
	@DisplayName("지원하는 OAuth 서비스가 없으면 언링크는 실행되지 않지만 삭제는 진행된다")
	void exit_Success_EvenIfNoProviderMatch() {
		//given
		Long userId = 1L;
		User user = new User();
		user.deleteRequested();

		unlinkServices.add(oAuth2UnlinkService);

		given(userReader.getUserById(userId)).willReturn(user);
		given(oAuth2UnlinkService.supports(any())).willReturn(false);

		//when
		userExitService.exit(userId);

		//then
		then(oAuth2UnlinkService).should(never()).unlink(user);
		assertThat(user.getUserStatus()).isEqualTo(UserStatus.DELETED);
	}
}