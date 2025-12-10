package com.recorday.recorday.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.auth.oauth2.service.OAuth2UnlinkService;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.enums.UserRole;
import com.recorday.recorday.user.enums.UserStatus;
import com.recorday.recorday.util.user.UserReader;

@ExtendWith(MockitoExtension.class)
class UserExitServiceImplTest {

	@Mock
	private UserReader userReader;

	@Mock
	private UserDeletionHandler deletionHandler1;

	@Mock
	private UserDeletionHandler deletionHandler2;

	@Mock
	private OAuth2UnlinkService unlinkService1;

	@Mock
	private OAuth2UnlinkService unlinkService2;

	private UserExitServiceImpl userExitService;

	Long userId;
	String publicId;
	User user;

	@BeforeEach
	void setUp() throws Exception {
		userId = 1L;
		publicId = "publicId";
		user = User.builder()
			.provider(Provider.RECORDAY)
			.userRole(UserRole.ROLE_USER)
			.email("test@example.com")
			.username("testUser")
			.password("1234")
			.profileUrl("/static/images/userDefaultImage.png")
			.userStatus(UserStatus.ACTIVE)
			.build();

		userExitService = new UserExitServiceImpl(
			userReader,
			List.of(deletionHandler1, deletionHandler2),
			List.of(unlinkService1, unlinkService2)
		);
	}

	@Test
	@DisplayName("탈퇴 시 User soft delete")
	void exit_softDeleteUser() {
		//given
		given(userReader.getUserById(userId)).willReturn(user);

		//when
		userExitService.exit(userId);

		//then
		assertThat(user.getUserStatus()).isEqualTo(UserStatus.DELETED);
		then(userReader).should(times(1)).getUserById(userId);

		then(deletionHandler1).should(times(1)).handleUserDeletion(userId);
		then(deletionHandler2).should(times(1)).handleUserDeletion(userId);
	}

	@Test
	@DisplayName("탈퇴 시 provider를 지원하는 OAuth2UnlinkService만 unlink 호출")
	void exit_callsUnlinkOnlyForSupportedProvider() {
		//given
		given(userReader.getUserById(userId)).willReturn(user);
		given(unlinkService1.supports(user.getProvider())).willReturn(false);
		given(unlinkService2.supports(user.getProvider())).willReturn(true);

		//when
		userExitService.exit(userId);

		//then
		assertThat(user.getUserStatus()).isEqualTo(UserStatus.DELETED);

		then(deletionHandler1).should(times(1)).handleUserDeletion(userId);
		then(deletionHandler2).should(times(1)).handleUserDeletion(userId);

		then(unlinkService1).should(never()).unlink(user);
		then(unlinkService2).should(times(1)).unlink(user);
	}
}