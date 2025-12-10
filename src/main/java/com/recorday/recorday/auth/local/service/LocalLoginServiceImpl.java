package com.recorday.recorday.auth.local.service;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.local.dto.request.LocalLoginRequest;
import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;
import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Deprecated
// @Service
@RequiredArgsConstructor
public class LocalLoginServiceImpl implements LocalLoginService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService jwtTokenService;

	@Override
	public AuthTokenResponse login(LocalLoginRequest request) {

		User user = userRepository.findByProviderAndEmail(Provider.RECORDAY, request.email())
			.orElseThrow(() -> new BusinessException(AuthErrorCode.NOT_EXIST_USER));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BusinessException(AuthErrorCode.WRONG_PASSWORD);
		}

		String accessToken = jwtTokenService.createAccessToken(user.getPublicId());
		String refreshToken = jwtTokenService.createRefreshToken(user.getPublicId());
		return new AuthTokenResponse(accessToken, refreshToken);
	}
}
