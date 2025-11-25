package com.recorday.recorday.auth.local.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.local.dto.request.LocalLoginRequest;
import com.recorday.recorday.auth.local.dto.response.LocalLoginResponse;
import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalAuthServiceImpl implements LocalAuthService{

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService jwtTokenService;

	@Override
	public LocalLoginResponse login(LocalLoginRequest request) {

		User user = userRepository.findByProviderAndEmail(Provider.RECORDAY, request.email())
			.orElseThrow(() -> new BusinessException(AuthErrorCode.NOT_EXIST_USER));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BusinessException(AuthErrorCode.WRONG_PASSWORD);
		}

		String accessToken = jwtTokenService.createAccessToken(user.getId());
		String refreshToken = jwtTokenService.createRefreshToken(user.getId());
		return new LocalLoginResponse(accessToken, refreshToken);
	}
}
