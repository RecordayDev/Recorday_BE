package com.recorday.recorday.auth.local.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.recorday.recorday.auth.entity.CustomUserPrincipal;
import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.jwt.dto.TokenResponse;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.jwt.service.RefreshTokenService;
import com.recorday.recorday.auth.local.dto.request.LocalLoginRequest;
import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;
import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.enums.UserRole;

@ExtendWith(MockitoExtension.class)
class LocalSecurityAuthServiceImplTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtTokenService jwtTokenService;

	@Mock
	private RefreshTokenService refreshTokenService;

	@InjectMocks
	private LocalSecurityLoginServiceImpl localSecurityAuthService;

	private LocalLoginRequest request;
	private CustomUserPrincipal principal;
	private Authentication authentication;

	@BeforeEach
	void setUp() throws Exception {
		String email = "test@example.com";
		String rawPassword = "1234";

		request = new LocalLoginRequest(email, rawPassword);
		User user = createLocalUser(email);

		principal = new CustomUserPrincipal(user);
		authentication = new UsernamePasswordAuthenticationToken(
			principal, null, principal.getAuthorities()
		);
	}

	@Test
	@DisplayName("Spring Security를 이용한 로그인 성공")
	void loginSuccess() {
		//given
		String email = "test@test.com";
		String password = "password";
		String publicId = "publicId";
		LocalLoginRequest request = new LocalLoginRequest(email, password);

		// Authentication Mocking
		Authentication authentication = mock(Authentication.class);
		CustomUserPrincipal principal = mock(CustomUserPrincipal.class);

		given(principal.getPublicId()).willReturn(publicId);
		given(authentication.getPrincipal()).willReturn(principal);

		given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
			.willReturn(authentication);

		given(jwtTokenService.createAccessToken(publicId)).willReturn("access-token");
		given(jwtTokenService.createRefreshToken(publicId)).willReturn("refresh-token");

		//when
		TokenResponse response = localSecurityAuthService.login(request);

		//then
		assertThat(response.accessToken()).isEqualTo("access-token");
		assertThat(response.refreshToken()).isEqualTo("refresh-token");

		then(authenticationManager).should().authenticate(any(UsernamePasswordAuthenticationToken.class));
		then(jwtTokenService).should().createAccessToken(publicId);
		then(jwtTokenService).should().createRefreshToken(publicId);
		then(refreshTokenService).should().saveRefreshToken(publicId, "refresh-token");
	}

	@Test
	@DisplayName("인증 실패 시 예외 발생")
	void loginFail_badCredentials() {
		//given
		given(authenticationManager.authenticate(any(Authentication.class)))
			.willThrow(new BadCredentialsException("bad credentials"));

		//then
		assertThatThrownBy(() -> localSecurityAuthService.login(request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_CREDENTIALS);

		then(authenticationManager).should().authenticate(any(Authentication.class));
		then(jwtTokenService).shouldHaveNoInteractions();
	}

	private User createLocalUser(String email) {
		return User.builder()
			.id(1L)
			.provider(Provider.RECORDAY)
			.userRole(UserRole.ROLE_USER)
			.email(email)
			.password("encoded")
			.username("테스트 유저")
			.profileUrl("resources/defaults/userDefaultImage.png")
			.build();
	}
  
}