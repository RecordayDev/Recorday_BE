package com.recorday.recorday.auth.local.service.mail;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.jwt.dto.TokenResponse;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.jwt.service.RefreshTokenService;
import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.repository.UserRepository;
import com.recorday.recorday.util.user.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailAuthService {

	private final MailAuthCodeService mailAuthCodeService;
	private final UserReader userReader;
	private final UserRepository userRepository;

	@Transactional
	public void sendAuthCode(String email) {

		if (!userRepository.existsByProviderAndEmail(Provider.RECORDAY, email)) {
			throw new BusinessException(AuthErrorCode.NOT_EXIST_USER);
		}

		mailAuthCodeService.sendCode(email);
	}

	@Transactional
	public void verifyAuthCode(String email, String inputCode) {

		mailAuthCodeService.verifyCode(email, inputCode);

		User user = userReader.getUserByEmailAndProvider(email, Provider.RECORDAY);

		user.emailAuthenticate();
	}
}
