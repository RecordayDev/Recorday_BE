package com.recorday.recorday.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recorday.recorday.auth.entity.CustomUserPrincipal;
import com.recorday.recorday.auth.local.dto.request.LocalChangePasswordRequest;
import com.recorday.recorday.auth.local.dto.request.LocalLoginRequest;
import com.recorday.recorday.auth.local.dto.request.LocalRegisterRequest;
import com.recorday.recorday.auth.local.dto.request.LocalResetPasswordRequest;
import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;
import com.recorday.recorday.auth.local.service.LocalLoginService;
import com.recorday.recorday.auth.local.service.LocalUserAuthService;
import com.recorday.recorday.auth.local.service.PasswordService;
import com.recorday.recorday.auth.service.UserExitService;
import com.recorday.recorday.util.response.Response;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LocalAuthController {

	private final LocalLoginService localLoginService;
	private final LocalUserAuthService localUserAuthService;
	private final UserExitService userExitService;
	private final PasswordService passwordService;

	@PostMapping("/recorday/login")
	public ResponseEntity<Response<AuthTokenResponse>> login(@RequestBody LocalLoginRequest request) {
		AuthTokenResponse tokenResponse = localLoginService.login(request);

		return Response.ok(tokenResponse).toResponseEntity();
	}

	@PostMapping("/recorday/register")
	public ResponseEntity<Response<Void>> register(@RequestBody LocalRegisterRequest request) {
		localUserAuthService.register(request);

		return Response.ok().toResponseEntity();
	}

	@DeleteMapping("/recorday/exit")
	public ResponseEntity<Response<Void>> exit(@AuthenticationPrincipal CustomUserPrincipal principal) {

		userExitService.exit(principal.getId());

		return Response.ok().toResponseEntity();
	}

	@PatchMapping("/recorday/reset/password")
	public ResponseEntity<Response<Void>> resetPassword(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@RequestBody LocalResetPasswordRequest request
	) {
		passwordService.resetPassword(principal.getId(), null, request.newPassword());

		return Response.ok().toResponseEntity();
	}

	@PatchMapping("/recorday/change/password")
	public ResponseEntity<Response<Void>> changePassword(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@RequestBody LocalChangePasswordRequest request
	) {
		passwordService.changePassword(principal.getId(), principal.getPassword(), request.oldPassword(), request.newPassword());

		return Response.ok().toResponseEntity();
	}

}
