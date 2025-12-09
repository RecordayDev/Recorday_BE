package com.recorday.recorday.auth.oauth2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.recorday.recorday.auth.entity.CustomOAuth2User;
import com.recorday.recorday.auth.oauth2.component.ProviderUserFactory;
import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.auth.oauth2.userinfo.ProviderUser;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.enums.UserRole;
import com.recorday.recorday.user.enums.UserStatus;
import com.recorday.recorday.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SocialLoginServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private SocialLoginService socialLoginService;

	@Test
	@DisplayName("기존 소셜 유저가 있으면 조회 후 반환 (Kakao - OIDC)")
	void processUser_existingUser_Kakao_OIDC() {
		// given
		String registrationId = "kakao";
		ClientRegistration clientRegistration = createClientRegistration(registrationId);

		OidcUser oidcUser = mock(OidcUser.class);
		Map<String, Object> attributes = Map.of("sub", "kakao-12345", "email", "kakao@test.com");

		OidcIdToken idToken = new OidcIdToken("token-value", Instant.now(), Instant.now().plusSeconds(60), attributes);

		given(oidcUser.getIdToken()).willReturn(idToken);

		Provider provider = Provider.from(registrationId);

		ProviderUser providerUser = mock(ProviderUser.class);
		given(providerUser.getProviderId()).willReturn("kakao-12345");
		given(providerUser.getAttributes()).willReturn(attributes);

		User existingUser = User.builder()
			.id(1L)
			.provider(provider)
			.providerId("kakao-12345")
			.userRole(UserRole.ROLE_USER)
			.email("kakao@test.com")
			.userStatus(UserStatus.ACTIVE)
			.build();

		given(userRepository.findByProviderAndProviderId(provider, "kakao-12345"))
			.willReturn(Optional.of(existingUser));

		try (MockedStatic<ProviderUserFactory> mockedStatic = mockStatic(ProviderUserFactory.class)) {
			mockedStatic.when(
				() -> ProviderUserFactory.of(provider, oidcUser, clientRegistration)
			).thenReturn(providerUser);

			// when
			CustomOAuth2User result = socialLoginService.processUser(clientRegistration, oidcUser);

			// then
			assertThat(result.getId()).isEqualTo(existingUser.getId());
			assertThat(result.getProvider()).isEqualTo(provider);

			assertThat(result.getIdToken()).isNotNull();
			assertThat(result.getIdToken().getTokenValue()).isEqualTo("token-value");

			then(userRepository).should(never()).save(any(User.class));
		}
	}

	@Test
	@DisplayName("신규 소셜 유저면 저장 후 반환 (Naver - OAuth2)")
	void processUser_newUser_register_Naver_OAuth2() {
		// given
		String registrationId = "naver";
		ClientRegistration clientRegistration = createClientRegistration(registrationId);

		OAuth2User oAuth2User = createOAuth2UserStub();

		Provider provider = Provider.from(registrationId);

		ProviderUser providerUser = mock(ProviderUser.class);
		given(providerUser.getProviderId()).willReturn("naver-response-id");
		given(providerUser.getEmail()).willReturn("new@naver.com");
		given(providerUser.getNickname()).willReturn("네이버유저");
		given(providerUser.getAttributes()).willReturn(oAuth2User.getAttributes());

		given(userRepository.findByProviderAndProviderId(provider, "naver-response-id"))
			.willReturn(Optional.empty());

		User savedUser = User.builder()
			.id(200L)
			.provider(provider)
			.providerId("naver-response-id")
			.email("new@naver.com")
			.userRole(UserRole.ROLE_USER)
			.userStatus(UserStatus.ACTIVE)
			.build();

		given(userRepository.save(any(User.class))).willReturn(savedUser);

		try (MockedStatic<ProviderUserFactory> mockedStatic = mockStatic(ProviderUserFactory.class)) {
			mockedStatic.when(
				() -> ProviderUserFactory.of(provider, oAuth2User, clientRegistration)
			).thenReturn(providerUser);

			// when
			CustomOAuth2User result = socialLoginService.processUser(clientRegistration, oAuth2User);

			// then
			assertThat(result.getId()).isEqualTo(savedUser.getId());
			assertThat(result.getProvider()).isEqualTo(provider);

			assertThat(result.getIdToken()).isNull();

			then(userRepository).should(times(1)).save(any(User.class));
		}
	}

	// Helper Methods
	private ClientRegistration createClientRegistration(String registrationId) {
		return ClientRegistration.withRegistrationId(registrationId)
			.clientId("test-client-id")
			.clientSecret("test-client-secret")
			.authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
			.scope("profile", "email") // 테스트용 임시 스코프
			.authorizationUri("https://example.com/oauth2/authorize")
			.tokenUri("https://example.com/oauth2/token")
			.userInfoUri("https://example.com/oauth2/userinfo")
			.userNameAttributeName("id") // 임시 키
			.clientName("Test Client")
			.build();
	}

	private OAuth2User createOAuth2UserStub() {
		Map<String, Object> attributes = Map.of(
			"id", "provider-123",
			"response", Map.of("id", "naver-response-id"), // 네이버 흉내
			"email", "test@example.com"
		);
		return new DefaultOAuth2User(
			Set.of(new SimpleGrantedAuthority("ROLE_USER")),
			attributes,
			"id"
		);
	}
}