package com.recorday.recorday.auth.oauth2.service;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.entity.CustomOAuth2User;
import com.recorday.recorday.auth.oauth2.component.ProviderUserFactory;
import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.auth.oauth2.userinfo.ProviderUser;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.enums.UserRole;
import com.recorday.recorday.user.enums.UserStatus;
import com.recorday.recorday.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialLoginService {

	private final UserRepository userRepository;

	@Transactional
	public CustomOAuth2User processUser(ClientRegistration clientRegistration, OAuth2User oAuth2User) {

		String registrationId = clientRegistration.getRegistrationId();
		Provider provider = Provider.from(registrationId);

		ProviderUser providerUser = ProviderUserFactory.of(provider, oAuth2User, clientRegistration);

		User user = userRepository
			.findByProviderAndProviderId(provider, providerUser.getProviderId())
			.orElseGet(() -> registerUser(provider, providerUser));

		String nameAttributeKey = clientRegistration.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();

		if (oAuth2User instanceof OidcUser oidcUser) {
			return new CustomOAuth2User(
				user,
				providerUser.getAttributes(),
				nameAttributeKey,
				provider,
				oidcUser.getIdToken(),
				oidcUser.getUserInfo()
			);
		}

		return new CustomOAuth2User(
			user,
			providerUser.getAttributes(),
			nameAttributeKey,
			provider
		);
	}

	private User registerUser(Provider provider, ProviderUser providerUser) {
		User user = User.builder()
			.provider(provider)
			.providerId(providerUser.getProviderId())
			.userRole(UserRole.ROLE_USER)
			.email(providerUser.getEmail())
			.username(providerUser.getNickname())
			.profileUrl("resources/defaults/userDefaultImage.png")
			.userStatus(UserStatus.ACTIVE)
			.build();

		return userRepository.save(user);
	}
}
