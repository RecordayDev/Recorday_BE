package com.recorday.recorday.frame.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recorday.recorday.auth.entity.CustomUserPrincipal;
import com.recorday.recorday.frame.dto.request.FrameCreateRequest;
import com.recorday.recorday.frame.service.FrameService;
import com.recorday.recorday.util.response.Response;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user")
public class FrameController {

	private final FrameService frameService;

	@PostMapping("/frame")
	public ResponseEntity<Response<Void>> createFrame(
		@RequestBody FrameCreateRequest request,
		@AuthenticationPrincipal CustomUserPrincipal principal
	) {

		frameService.createFrame(principal.getId(), request);

		return Response.ok().toResponseEntity();
	}
}
