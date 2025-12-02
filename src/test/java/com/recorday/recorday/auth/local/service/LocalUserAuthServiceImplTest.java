package com.recorday.recorday.auth.local.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.local.dto.request.LocalRegisterRequest;
import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.enums.UserRole;
import com.recorday.recorday.user.enums.UserStatus;
import com.recorday.recorday.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class LocalUserAuthServiceImplTest {

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private LocalUserAuthServiceImpl localUserAuthService;

	private LocalRegisterRequest localRegisterRequest;

	@BeforeEach
	void setUp() throws Exception {

		localRegisterRequest = new LocalRegisterRequest(
			"test@example.com",
			"encoded-password",
			"testUser"
		);

	}

	@Test
	@DisplayName("자체 회원가입 이메일 중복")
	void localRegister_emailDuplicated() {
		//given
		given(userRepository.existsByProviderAndEmail(Provider.RECORDAY, localRegisterRequest.email()))
			.willReturn(true);

		//when
		BusinessException exception = assertThrows(BusinessException.class,
			() -> localUserAuthService.register(localRegisterRequest));

		//then
		assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.EMAIL_DUPLICATED);

		then(userRepository).should(times(1))
			.existsByProviderAndEmail(Provider.RECORDAY, localRegisterRequest.email());
		then(userRepository).should(never()).save(any(User.class));
	}

	@Test
	@DisplayName("자체 회원가입 성공")
	void localRegister_success() {
		//given
		given(userRepository.existsByProviderAndEmail(Provider.RECORDAY, localRegisterRequest.email()))
			.willReturn(false);

		given(passwordEncoder.encode(localRegisterRequest.password()))
			.willReturn("encoded-password");

		given(userRepository.save(any(User.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		//when
		localUserAuthService.register(localRegisterRequest);

		//then
		then(userRepository).should(times(1))
			.existsByProviderAndEmail(Provider.RECORDAY, localRegisterRequest.email());

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		then(userRepository).should(times(1)).save(userCaptor.capture());

		User userArgument = userCaptor.getValue();
		assertThat(userArgument.getProvider()).isEqualTo(Provider.RECORDAY);
		assertThat(userArgument.getUserRole()).isEqualTo(UserRole.ROLE_USER);
		assertThat(userArgument.getEmail()).isEqualTo(localRegisterRequest.email());
		assertThat(userArgument.getUsername()).isEqualTo(localRegisterRequest.username());
		assertThat(userArgument.getPassword()).isEqualTo(localRegisterRequest.password());
		assertThat(userArgument.getProfileUrl()).isEqualTo("/static/images/userDefaultImage.png");
		assertThat(userArgument.getDeleted()).isEqualTo(UserStatus.ACTIVE);
	}

}