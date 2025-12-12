package com.recorday.recorday.auth.local.service.mail;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.component.VerificationCodeGenerator;
import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.repository.VerificationTokenRepository;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.mail.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailAuthCodeService {

	private final VerificationCodeGenerator generator;
	private final VerificationTokenRepository repository;
	private final EmailService emailService;

	private static final long VERIFICATION_TTL = 300L;
	private static final String EMAIL_SUBJECT = "[Recorday] 이메일 인증 코드입니다.";

	public void sendCode(String email) {

		String code = generator.generate();

		repository.save(email, code, VERIFICATION_TTL);

		emailService.sendVerificationCode(email, EMAIL_SUBJECT, code);
	}

	public void verifyCode(String email, String inputCode) {

		String storedCode = repository.getCode(email);

		if (storedCode == null || !storedCode.equals(inputCode)) {
			throw new BusinessException(AuthErrorCode.EMAIL_AUTH_FAILED);
		}

		repository.remove(email);
	}

}
