package com.recorday.recorday.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.jwt.dto.AuthCodePayload;
import com.recorday.recorday.auth.jwt.dto.TokenResponse;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.jwt.service.RefreshTokenService;
import com.recorday.recorday.auth.oauth2.service.AuthCodeService;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.repository.UserRepository;
import com.recorday.recorday.util.user.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationCodeTokenServiceImpl implements AuthorizationCodeTokenService {

	private final AuthCodeService authCodeService;
	private final JwtTokenService jwtTokenService;
	private final RefreshTokenService refreshTokenService;
	private final UserReader userReader;

	@Override
	@Transactional
	public TokenResponse issueTokens(String code) {

		AuthCodePayload authPayload = authCodeService.getAuthPayload(code);
		String publicId = authPayload.publicId();

		User user = userReader.getUserByPublicId(publicId);

		String accessToken = jwtTokenService.createAccessToken(publicId);
		String refreshToken = jwtTokenService.createRefreshToken(publicId);

		authCodeService.deleteAuthCode(code);
		refreshTokenService.saveRefreshToken(publicId, refreshToken);

		return new TokenResponse(accessToken, refreshToken, user.getUserStatus());
	}
}
