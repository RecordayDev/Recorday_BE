package com.recorday.recorday.auth.oauth2.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
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
class CustomOAuth2UserServiceTest {

	@Mock
	private UserRepository userRepository;

	private CustomOAuth2UserService customOAuth2UserService;

	@BeforeEach
	void setUp() throws Exception {
		customOAuth2UserService = new CustomOAuth2UserService(userRepository);
	}

	@Test
	@DisplayName("기존 소셜 유저가 있으면 CustomOAuth2User 반환")
	void loadUser_existingUser_Kakao() {
		//given
		String registrationId = "kakao";
		ClientRegistration clientRegistration = createClientRegistration(registrationId);
		OAuth2UserRequest userRequest = createUserRequest(clientRegistration);
		OAuth2User oAuth2User = createOAuth2UserStub();

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
			.password(null)
			.username("테스터")
			.profileUrl("https://example.com/profile.png")
			.deleted(UserStatus.ACTIVE)
			.build();

		given(userRepository.findByProviderAndProviderId(provider, "provider-123"))
			.willReturn(Optional.of(existingUser));

		try (MockedStatic<ProviderUserFactory> mockedStatic = mockStatic(ProviderUserFactory.class)) {
			mockedStatic.when(
				() -> ProviderUserFactory.of(provider, oAuth2User, clientRegistration)
			).thenReturn(providerUser);

			//when
			OAuth2User result = customOAuth2UserService.processUser(userRequest, oAuth2User);

			//then
			assertThat(result).isInstanceOf(CustomOAuth2User.class);
			CustomOAuth2User customUser = (CustomOAuth2User) result;

			assertThat(customUser.getId()).isEqualTo(existingUser.getId());
			assertThat(customUser.getEmail()).isEqualTo(existingUser.getEmail());
			assertThat(customUser.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.toList())
				.containsExactly(existingUser.getUserRole().name());

			assertThat(customUser.getAttributes()).isEqualTo(oAuth2User.getAttributes());
			assertThat(customUser.getProvider()).isEqualTo(provider);
			assertThat(customUser.getName()).isEqualTo("provider-123");

			then(userRepository).should(never()).save(any(User.class));
		}
	}

	@Test
	@DisplayName("신규 소셜 유저면 저장 후 CustomOAuth2User 반환")
	void loadUser_newUser_register_Kakao() {
		//given
		String registrationId = "kakao";
		ClientRegistration clientRegistration = createClientRegistration(registrationId);
		OAuth2UserRequest userRequest = createUserRequest(clientRegistration);
		OAuth2User oAuth2User = createOAuth2UserStub();

		Provider provider = Provider.from(registrationId);

		ProviderUser providerUser = mock(ProviderUser.class);
		given(providerUser.getProviderId()).willReturn("provider-456");
		given(providerUser.getEmail()).willReturn("new@example.com");
		given(providerUser.getNickname()).willReturn("뉴유저");
		given(providerUser.getProfileImageUrl()).willReturn("https://example.com/new.png");
		given(providerUser.getAttributes()).willReturn(oAuth2User.getAttributes());

		given(userRepository.findByProviderAndProviderId(provider, "provider-456"))
			.willReturn(Optional.empty());

		User savedUser = User.builder()
			.id(100L)
			.provider(provider)
			.providerId("provider-456")
			.userRole(UserRole.ROLE_USER)
			.email("new@example.com")
			.password(null)
			.username("뉴유저")
			.profileUrl("https://example.com/new.png")
			.deleted(UserStatus.ACTIVE)
			.build();

		given(userRepository.save(any(User.class))).willReturn(savedUser);

		try (MockedStatic<ProviderUserFactory> mockedStatic = mockStatic(ProviderUserFactory.class)) {

			mockedStatic.when(
				() -> ProviderUserFactory.of(provider, oAuth2User, clientRegistration)
			).thenReturn(providerUser);

			//when
			OAuth2User result = customOAuth2UserService.processUser(userRequest, oAuth2User);

			//then
			assertThat(result).isInstanceOf(CustomOAuth2User.class);
			CustomOAuth2User customUser = (CustomOAuth2User) result;

			assertThat(customUser.getId()).isEqualTo(savedUser.getId());
			assertThat(customUser.getEmail()).isEqualTo(savedUser.getEmail());

			assertThat(customUser.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.toList())
				.containsExactly(savedUser.getUserRole().name());

			assertThat(customUser.getAttributes()).isEqualTo(oAuth2User.getAttributes());
			assertThat(customUser.getProvider()).isEqualTo(provider);
			then(userRepository).should(times(1)).save(any(User.class));
		}
	}

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
			.userNameAttributeName("id")
			.clientName("Test Client")
			.build();
	}

	private OAuth2UserRequest createUserRequest(ClientRegistration clientRegistration) {
		OAuth2AccessToken accessToken = new OAuth2AccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			"access-token-value",
			null,
			null
		);

		return new OAuth2UserRequest(clientRegistration, accessToken);
	}

	private OAuth2User createOAuth2UserStub() {
		Map<String, Object> attributes = Map.of(
			"id", "provider-123",
			"email", "test@example.com",
			"nickname", "테스터",
			"profile_image", "https://example.com/profile.png"
		);

		return new DefaultOAuth2User(
			Set.of(new SimpleGrantedAuthority("ROLE_USER")),
			attributes,
			"id"
		);
	}

}