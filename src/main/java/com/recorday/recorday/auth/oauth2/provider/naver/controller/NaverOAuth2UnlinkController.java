package com.recorday.recorday.auth.oauth2.provider.naver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.recorday.recorday.auth.oauth2.provider.naver.dto.NaverUnlinkRequest;
import com.recorday.recorday.auth.oauth2.provider.naver.service.NaverOAuth2UnlinkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NaverOAuth2UnlinkController {

	private final NaverOAuth2UnlinkService naverOAuth2UnlinkService;

	@PostMapping("/api/oauth2/unlink/naver")
	public ResponseEntity<Void> unlink(@RequestBody NaverUnlinkRequest request) {

		try {
			naverOAuth2UnlinkService.unlink(request);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}

		return ResponseEntity.noContent().build();
	}
}
