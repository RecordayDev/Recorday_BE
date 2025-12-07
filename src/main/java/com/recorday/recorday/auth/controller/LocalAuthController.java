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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth (Local)", description = "이메일/비밀번호 기반 자체 로그인 및 회원 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LocalAuthController {

	private final LocalLoginService localLoginService;
	private final LocalUserAuthService localUserAuthService;
	private final UserExitService userExitService;
	private final PasswordService passwordService;

	@Operation(summary = "이메일 로그인", description = "등록된 이메일과 비밀번호로 로그인하여 Access/Refresh 토큰을 발급받습니다.")
	@PostMapping("/recorday/login")
	public ResponseEntity<Response<AuthTokenResponse>> login(@RequestBody @Valid LocalLoginRequest request) {
		AuthTokenResponse tokenResponse = localLoginService.login(request);
		return Response.ok(tokenResponse).toResponseEntity();
	}

	@Operation(summary = "이메일 회원가입", description = "새로운 사용자를 등록합니다.")
	@PostMapping("/recorday/register")
	public ResponseEntity<Response<Void>> register(@RequestBody @Valid LocalRegisterRequest request) {
		localUserAuthService.register(request);
		return Response.ok().toResponseEntity();
	}

	@Operation(summary = "회원 탈퇴", description = "현재 로그인된 사용자의 계정을 삭제하고 탈퇴 처리합니다.")
	@DeleteMapping("/recorday/exit")
	public ResponseEntity<Response<Void>> exit(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		userExitService.exit(principal.getId());
		return Response.ok().toResponseEntity();
	}

	@Operation(summary = "비밀번호 재설정 (강제 변경)", description = "기존 비밀번호 확인 없이 새로운 비밀번호로 변경합니다. (주로 비밀번호 찾기 인증 후 사용)")
	@PatchMapping("/recorday/reset/password")
	public ResponseEntity<Response<Void>> resetPassword(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@RequestBody LocalResetPasswordRequest request
	) {
		passwordService.resetPassword(principal.getId(), null, request.newPassword());
		return Response.ok().toResponseEntity();
	}

	@Operation(summary = "비밀번호 변경", description = "기존 비밀번호를 검증한 후, 일치하면 새로운 비밀번호로 변경합니다.")
	@PatchMapping("/recorday/change/password")
	public ResponseEntity<Response<Void>> changePassword(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@RequestBody LocalChangePasswordRequest request
	) {
		passwordService.changePassword(principal.getId(), principal.getPassword(), request.oldPassword(), request.newPassword());
		return Response.ok().toResponseEntity();
	}
}