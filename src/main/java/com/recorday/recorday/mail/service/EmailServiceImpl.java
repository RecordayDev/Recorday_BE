package com.recorday.recorday.mail.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.exception.BusinessException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

	private final JavaMailSender javaMailSender;
	private final SpringTemplateEngine templateEngine;

	@Override
	public void sendVerificationCode(String email, String subject, String code) {

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();

		try {

			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

			helper.setTo(email);
			helper.setSubject(subject);

			Context context = new Context();
			context.setVariable("code", code);

			String htmlContent = templateEngine.process("verification-code", context);

			helper.setText(htmlContent, true);

			javaMailSender.send(mimeMessage);
			log.info("Email sent successfully to: {}", email);

		} catch (MessagingException e) {
			log.error("Failed to send email", e);
			throw new BusinessException(AuthErrorCode.EMAIL_SEND_FAILED);
		}
	}
}
