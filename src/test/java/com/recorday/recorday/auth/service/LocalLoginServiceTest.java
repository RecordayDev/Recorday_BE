package com.recorday.recorday.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.local.dto.request.LocalLoginRequest;
import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;
import com.recorday.recorday.auth.local.service.LocalLoginServiceImpl;
import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.enums.UserRole;
import com.recorday.recorday.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class LocalLoginServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenService jwtTokenService;

	@InjectMocks
	private LocalLoginServiceImpl localAuthService;

	private String email;
	private String rawPassword;
	private String encodedPassword;
	private LocalLoginRequest request;
	private User user;

	@BeforeEach
	void setUp() throws Exception {
		email = "test@example.com";
		rawPassword = "1234";
		encodedPassword = "$2a$10$encoded-password";

		request = new LocalLoginRequest(email, rawPassword);
		user = createLocalUser(email, encodedPassword);
	}

	@Test
	@DisplayName("로컬 로그인 성공")
	void loginSuccess() {
		//given
		given(userRepository.findByProviderAndEmail(Provider.RECORDAY, email))
			.willReturn(Optional.of(user));
		given(passwordEncoder.matches(rawPassword, encodedPassword))
			.willReturn(true);
		given(jwtTokenService.createAccessToken(user.getId()))
			.willReturn("access-token");

		//when
		AuthTokenResponse response = localAuthService.login(request);

		//then
		assertThat(response.accessToken()).isEqualTo("access-token");

		then(userRepository).should().findByProviderAndEmail(Provider.RECORDAY, email);
		then(passwordEncoder).should().matches(rawPassword, encodedPassword);
		then(jwtTokenService).should().createAccessToken(user.getId());
	}

	@Test
	@DisplayName("유저가 존재하지 않으면 로그인 실패")
	void loginFail_userNotFound() {
		//given
		given(userRepository.findByProviderAndEmail(Provider.RECORDAY, email))
			.willReturn(Optional.empty());

		//then
		assertThatThrownBy(() -> localAuthService.login(request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.NOT_EXIST_USER);

		then(userRepository).should().findByProviderAndEmail(Provider.RECORDAY, email);
	}

	@Test
	@DisplayName("비밀번호 틀림")
	void loginFail_wrongPassword() {
		//given
		given(userRepository.findByProviderAndEmail(Provider.RECORDAY, email))
			.willReturn(Optional.of(user));
		given(passwordEncoder.matches(rawPassword, encodedPassword))
			.willReturn(false);

		//then
		assertThatThrownBy(() -> localAuthService.login(request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.WRONG_PASSWORD);

		then(userRepository).should().findByProviderAndEmail(Provider.RECORDAY, email);
		then(passwordEncoder).should().matches(rawPassword, encodedPassword);
	}

	private User createLocalUser(String email, String encodedPassword) {
		return User.builder()
			.provider(Provider.RECORDAY)
			.userRole(UserRole.ROLE_USER)
			.email(email)
			.password(encodedPassword)
			.username("테스트 유저")
			.profileUrl("https://example.com/profile.png")
			.build();
	}
}