package com.recorday.recorday.frame.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recorday.recorday.auth.entity.CustomUserPrincipal;
import com.recorday.recorday.frame.dto.request.FrameCreateRequest;
import com.recorday.recorday.frame.dto.response.FrameResponse;
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

	@GetMapping("/frame")
	public ResponseEntity<Response<List<FrameResponse>>> getMyFrames(
		@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		List<FrameResponse> response = frameService.getMyFrame(principal.getId());

		return Response.ok(response).toResponseEntity();
	}

	@GetMapping("/frame/{frameId}")
	public ResponseEntity<Response<FrameResponse>> getFrame(
		@PathVariable Long frameId,
		@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		FrameResponse response = frameService.getFrame(frameId, principal.getId());

		return Response.ok(response).toResponseEntity();
	}

	@DeleteMapping("/frame/{frameId}")
	public ResponseEntity<Response<Void>> deleteFrame(
		@PathVariable Long frameId,
		@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		frameService.deleteFrame(principal.getId(), frameId);
		return Response.ok().toResponseEntity();
	}

	@PutMapping("/frame/{frameId}")
	public ResponseEntity<Response<Void>> updateFrame(
		@PathVariable Long frameId,
		@RequestBody FrameCreateRequest request,
		@AuthenticationPrincipal CustomUserPrincipal principal
	) {
		frameService.updateFrame(principal.getId(), frameId, request);
		return Response.ok().toResponseEntity();
	}
}
