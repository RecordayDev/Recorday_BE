package com.recorday.recorday.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.jwt.dto.AuthCodePayload;
import com.recorday.recorday.auth.jwt.dto.TokenResponse;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.oauth2.service.AuthCodeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationCodeTokenServiceImpl implements AuthorizationCodeTokenService {

	private final AuthCodeService authCodeService;
	private final JwtTokenService jwtTokenService;

	@Override
	@Transactional
	public TokenResponse issueTokens(String code) {

		AuthCodePayload authPayload = authCodeService.getAuthPayload(code);
		Long userId = authPayload.userId();

		String accessToken = jwtTokenService.createAccessToken(userId);
		String refreshToken = jwtTokenService.createRefreshToken(userId);

		authCodeService.deleteAuthCode(code);

		return new TokenResponse(accessToken, refreshToken);
	}
}
