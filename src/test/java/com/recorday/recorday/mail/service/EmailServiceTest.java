package com.recorday.recorday.mail.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	@Mock
	private JavaMailSender javaMailSender;

	@InjectMocks
	private EmailServiceImpl emailService;

	@Test
	@DisplayName("이메일, 제목, 인증코드가 주어지면 메일이 전송되어야 한다")
	void sendVerificationCode_test() {
		//given
		String to = "user@example.com";
		String subject = "이메일 인증";
		String code = "123456";

		//when
		emailService.sendVerificationCode(to, subject, code);

		//then
		ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

		then(javaMailSender).should().send(messageCaptor.capture());

		SimpleMailMessage capturedMessage = messageCaptor.getValue();

		assertThat(capturedMessage.getTo()).containsExactly(to);
		assertThat(capturedMessage.getSubject()).isEqualTo(subject);
		assertThat(capturedMessage.getText()).contains(code);
	}

}