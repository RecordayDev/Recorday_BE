package com.recorday.recorday.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recorday.recorday.auth.jwt.dto.TokenResponse;
import com.recorday.recorday.auth.local.dto.request.EmailAuthRequest;
import com.recorday.recorday.auth.local.dto.request.EmailAuthVerifyRequest;
import com.recorday.recorday.auth.local.service.mail.MailAuthCodeService;
import com.recorday.recorday.auth.local.service.mail.MailAuthService;
import com.recorday.recorday.common.annotation.PreventDuplicateRequest;
import com.recorday.recorday.util.response.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "이메일 인증 API", description = "회원가입 후 이메일 인증 코드 발송 및 검증을 담당하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email-auth")
public class MailAuthController {

	private final MailAuthService mailAuthService;

	@Operation(
		summary = "인증 코드 발송",
		description = "사용자의 이메일로 6자리 인증 코드를 전송합니다. 코드는 5분간 유효합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
		@ApiResponse(responseCode = "500", description = "메일 전송 실패 (서버 에러)")
	})
	@PreventDuplicateRequest(key = "#request.email")
	@PostMapping("/code")
	public ResponseEntity<Response<Void>> sendAuthCode(@RequestBody @Valid EmailAuthRequest request) {

		mailAuthService.sendAuthCode(request.email());

		return Response.ok().toResponseEntity();
	}

	@Operation(
		summary = "인증 코드 검증",
		description = "이메일과 인증 코드를 검증합니다. 성공 시 사용자 상태를 활성화(ACTIVE)"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "검증 성공 및 토큰 발급 완료"),
		@ApiResponse(responseCode = "400", description = "인증 실패 (코드가 일치하지 않거나 만료됨)"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
	})
	@PreventDuplicateRequest(key = "#request.email", time = 2000)
	@PostMapping("/verification")
	public ResponseEntity<Response<Void>> verifyAuthCode(@RequestBody @Valid EmailAuthVerifyRequest request) {

		mailAuthService.verifyAuthCode(request.email(), request.code());

		return Response.ok().toResponseEntity();
	}


}
