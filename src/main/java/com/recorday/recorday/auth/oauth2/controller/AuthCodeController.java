package com.recorday.recorday.auth.oauth2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recorday.recorday.auth.jwt.dto.TokenResponse;
import com.recorday.recorday.auth.service.AuthorizationCodeTokenService;
import com.recorday.recorday.util.response.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth (OAuth2)", description = "소셜 로그인(OAuth2) 인증 코드 처리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthCodeController {

	private final AuthorizationCodeTokenService authorizationCodeTokenService;

	@Operation(summary = "OAuth2 인증 코드로 토큰 발급", description = "OAuth Provider(구글, 카카오 등)로부터 받은 Authorization Code를 사용하여 서비스 전용 Access/Refresh Token을 발급받습니다.")
	@PostMapping("/oauth2/authorize/complete")
	public ResponseEntity<Response<TokenResponse>> authCode(
		@Parameter(description = "OAuth 인증 후 발급받은 Authorization Code", required = true, example = "4/P7q7W91a-oMsCeLvIaQm6bTrgtp7")
		@RequestParam String code
	) {
		TokenResponse tokenResponse = authorizationCodeTokenService.issueTokens(code);

		return Response.ok(tokenResponse).toResponseEntity();
	}
}