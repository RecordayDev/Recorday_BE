package com.recorday.recorday.auth.oauth2.service;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.recorday.recorday.auth.entity.CustomOAuth2User;
import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.exception.CustomAuthenticationException;
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
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		OAuth2User oAuth2User = super.loadUser(userRequest);

		return processUser(userRequest, oAuth2User);
	}

	public OAuth2User processUser(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {

		ClientRegistration clientRegistration = userRequest.getClientRegistration();
		String registrationId = clientRegistration.getRegistrationId();
		Provider provider = Provider.from(registrationId);

		ProviderUser providerUser = ProviderUserFactory.of(provider, oAuth2User, clientRegistration);

		User user = userRepository
			.findByProviderAndProviderId(provider, providerUser.getProviderId())
			.orElseGet(() -> registerOAuth2User(provider, providerUser));

		// if (user.getUserStatus().equals(UserStatus.DELETED_REQUESTED)) {
		// 	throw new OAuth2AuthenticationException(
		// 		new OAuth2Error(AuthErrorCode.DELETED_USER.getCode()), AuthErrorCode.DELETED_USER.getMessage()
		// 	);
		// }

		String nameAttributeKey = clientRegistration.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();

		return new CustomOAuth2User(
			user,
			providerUser.getAttributes(),
			nameAttributeKey,
			provider
		);

	}

	private User registerOAuth2User(Provider provider, ProviderUser providerUser) {

		User user = User.builder()
			.provider(provider)
			.providerId(providerUser.getProviderId())
			.userRole(UserRole.ROLE_USER)
			.email(providerUser.getEmail())
			.password(null)
			.username(providerUser.getNickname())
			.profileUrl(providerUser.getProfileImageUrl())
			.userStatus(UserStatus.ACTIVE)
			.build();

		return userRepository.save(user);
	}
}
