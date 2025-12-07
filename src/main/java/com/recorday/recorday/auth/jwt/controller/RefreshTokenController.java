package com.recorday.recorday.auth.jwt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recorday.recorday.auth.entity.CustomUserPrincipal;
import com.recorday.recorday.auth.jwt.service.RefreshTokenService;
import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;
import com.recorday.recorday.util.response.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth (Token)", description = "JWT 토큰 재발급 및 로그아웃 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RefreshTokenController {

	private final RefreshTokenService refreshTokenService;

	@Operation(summary = "토큰 재발급", description = "헤더에 포함된 Refresh Token을 검증하여 새로운 Access Token과 Refresh Token을 발급합니다.")
	@PostMapping("/recorday/reissue")
	public ResponseEntity<Response<AuthTokenResponse>> reissue(
		@Parameter(description = "리프레시 토큰 (Bearer 없이 토큰 값만 입력)", required = true, example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
		@RequestHeader("Refresh-Token") String refreshToken
	) {
		AuthTokenResponse tokens = refreshTokenService.reissue(refreshToken);
		return Response.ok(tokens).toResponseEntity();
	}

	@Operation(summary = "로그아웃", description = "서버(Redis)에 저장된 해당 사용자의 Refresh Token을 삭제하여 로그아웃 처리합니다.")
	@DeleteMapping("/recorday/logout")
	public ResponseEntity<Response<Void>> logout(
		@Parameter(hidden = true) // 사용자 정보는 SecurityContext에서 가져오므로 문서에서 숨김
		@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		refreshTokenService.logout(principal.getPublicId());
		return Response.ok().toResponseEntity();
	}
}