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

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RefreshTokenController {

	private final RefreshTokenService refreshTokenService;

	@PostMapping("/recorday/reissue")
	public ResponseEntity<Response<AuthTokenResponse>> reissue(
		@RequestHeader("Refresh-Token") String refreshToken
	) {
		AuthTokenResponse tokens = refreshTokenService.reissue(refreshToken);
		return Response.ok(tokens).toResponseEntity();
	}

	@DeleteMapping("/recorday/logout")
	public ResponseEntity<Response<Void>> logout(@AuthenticationPrincipal CustomUserPrincipal principal) {

		refreshTokenService.logout(principal.getPublicId());

		return Response.ok().toResponseEntity();
	}
}
