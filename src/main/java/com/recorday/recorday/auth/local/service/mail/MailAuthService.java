package com.recorday.recorday.auth.local.service.mail;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.jwt.dto.TokenResponse;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.jwt.service.RefreshTokenService;
import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.util.user.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailAuthService {

	private final MailAuthCodeService mailAuthCodeService;
	private final UserReader userReader;
	private final JwtTokenService jwtTokenService;
	private final RefreshTokenService refreshTokenService;

	@Transactional
	public void sendAuthCode(String email) {

		mailAuthCodeService.sendCode(email);
	}

	@Transactional
	public TokenResponse verifyAuthCode(String email, String inputCode) {

		mailAuthCodeService.verifyCode(email, inputCode);

		User user = userReader.getUserByEmailAndProvider(email, Provider.RECORDAY);

		user.emailAuthenticate();

		String accessToken = jwtTokenService.createAccessToken(user.getPublicId());
		String refreshToken = jwtTokenService.createRefreshToken(user.getPublicId());

		refreshTokenService.saveRefreshToken(user.getPublicId(), refreshToken);

		return new TokenResponse(accessToken, refreshToken);
	}
}
