package com.recorday.recorday.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	@Mock
	private JavaMailSender javaMailSender;

	@Mock
	private SpringTemplateEngine templateEngine;

	@Mock
	private MimeMessage mimeMessage;

	@InjectMocks
	private EmailServiceImpl emailService;

	@Test
	@DisplayName("이메일, 제목, 인증코드가 주어지면 HTML 템플릿을 사용하여 메일이 전송되어야 한다")
	void sendVerificationCode_test() {
		// given
		String to = "user@example.com";
		String subject = "이메일 인증";
		String code = "123456";
		String htmlContent = "<html>...</html>";

		// 1. MimeMessage 생성 시 Mock 객체 반환 설정
		given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);

		// 2. 템플릿 엔진 동작 설정 (어떤 Context가 들어오든 htmlContent 반환)
		given(templateEngine.process(eq("verification-code"), any(Context.class)))
			.willReturn(htmlContent);

		// when
		emailService.sendVerificationCode(to, subject, code);

		// then
		// 1. JavaMailSender가 Mock MimeMessage를 전송했는지 검증
		then(javaMailSender).should().send(mimeMessage);

		// 2. TemplateEngine에 전달된 Context 변수(인증코드) 검증
		ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

		then(templateEngine).should().process(eq("verification-code"), contextCaptor.capture());

		Context capturedContext = contextCaptor.getValue();
		assertThat(capturedContext.getVariable("code")).isEqualTo(code);
	}
}