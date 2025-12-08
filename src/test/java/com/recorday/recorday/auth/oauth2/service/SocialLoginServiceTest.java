package com.recorday.recorday.auth.oauth2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

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
	private SocialLoginService socialLoginService; // [변경] 타겟 서비스 변경

	@Test
	@DisplayName("기존 소셜 유저가 있으면 조회 후 CustomOAuth2User 반환 (Kakao)")
	void processUser_existingUser_Kakao() {
		//given
		String registrationId = "kakao";
		ClientRegistration clientRegistration = createClientRegistration(registrationId);
		OAuth2User oAuth2User = createOAuth2UserStub(); // Kakao는 일반 OAuth2User

		Provider provider = Provider.from(registrationId);

		ProviderUser providerUser = mock(ProviderUser.class);
		given(providerUser.getProviderId()).willReturn("provider-123");
		given(providerUser.getAttributes()).willReturn(oAuth2User.getAttributes());

		User existingUser = User.builder()
			.id(1L)
			.provider(provider)
			.providerId("provider-123")
			.userRole(UserRole.ROLE_USER)
			.email("test@example.com")
			.userStatus(UserStatus.ACTIVE)
			.build();

		given(userRepository.findByProviderAndProviderId(provider, "provider-123"))
			.willReturn(Optional.of(existingUser));

		try (MockedStatic<ProviderUserFactory> mockedStatic = mockStatic(ProviderUserFactory.class)) {
			mockedStatic.when(
				() -> ProviderUserFactory.of(provider, oAuth2User, clientRegistration)
			).thenReturn(providerUser);

			//when
			// [변경] 메서드 시그니처가 변경되었습니다 (Request -> ClientRegistration)
			CustomOAuth2User result = socialLoginService.processUser(clientRegistration, oAuth2User);

			//then
			assertThat(result.getId()).isEqualTo(existingUser.getId());
			assertThat(result.getEmail()).isEqualTo(existingUser.getEmail());
			assertThat(result.getProvider()).isEqualTo(provider);

			// OIDC 필드가 null인지 확인 (Kakao니까)
			assertThat(result.getIdToken()).isNull();

			then(userRepository).should(never()).save(any(User.class));
		}
	}

	@Test
	@DisplayName("신규 소셜 유저면 저장 후 CustomOAuth2User 반환 (Google - OIDC)")
	void processUser_newUser_register_Google_OIDC() {
		//given
		String registrationId = "google";
		ClientRegistration clientRegistration = createClientRegistration(registrationId);

		// [중요] OIDC User Mocking
		OidcUser oidcUser = mock(OidcUser.class);
		Map<String, Object> attributes = Map.of("sub", "google-123", "email", "new@google.com");

		given(oidcUser.getAttributes()).willReturn(attributes);
		// OidcUser는 getIdToken()을 호출할 수 있어야 함
		given(oidcUser.getIdToken()).willReturn(null); // 테스트 편의상 null, 실제로는 객체 필요

		Provider provider = Provider.from(registrationId);

		ProviderUser providerUser = mock(ProviderUser.class);
		given(providerUser.getProviderId()).willReturn("google-123");
		given(providerUser.getEmail()).willReturn("new@google.com");
		given(providerUser.getNickname()).willReturn("GoogleUser");
		given(providerUser.getAttributes()).willReturn(attributes);

		given(userRepository.findByProviderAndProviderId(provider, "google-123"))
			.willReturn(Optional.empty());

		User savedUser = User.builder()
			.id(200L)
			.provider(provider)
			.providerId("google-123")
			.email("new@google.com")
			.userRole(UserRole.ROLE_USER)
			.userStatus(UserStatus.ACTIVE)
			.build();

		given(userRepository.save(any(User.class))).willReturn(savedUser);

		try (MockedStatic<ProviderUserFactory> mockedStatic = mockStatic(ProviderUserFactory.class)) {
			mockedStatic.when(
				() -> ProviderUserFactory.of(provider, oidcUser, clientRegistration)
			).thenReturn(providerUser);

			//when
			CustomOAuth2User result = socialLoginService.processUser(clientRegistration, oidcUser);

			//then
			assertThat(result.getId()).isEqualTo(savedUser.getId());
			assertThat(result.getProvider()).isEqualTo(provider);

			// OIDC User로 들어왔으므로 CustomOAuth2User 생성 시 OIDC 생성자를 탔는지 검증 가능
			// (getIdToken 호출 여부 등으로 간접 확인하거나, 리턴된 객체의 필드 확인)

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
			.scope("profile", "email")
			.authorizationUri("https://example.com/oauth2/authorize")
			.tokenUri("https://example.com/oauth2/token")
			.userInfoUri("https://example.com/oauth2/userinfo")
			.userNameAttributeName("sub") // Google 기준
			.clientName("Test Client")
			.build();
	}

	private OAuth2User createOAuth2UserStub() {
		Map<String, Object> attributes = Map.of(
			"id", "provider-123",
			"email", "test@example.com",
			"nickname", "테스터",
			"profile_image", "resources/defaults/userDefaultImage.png"
		);
		return new DefaultOAuth2User(
			Set.of(new SimpleGrantedAuthority("ROLE_USER")),
			attributes,
			"id"
		);
	}
}