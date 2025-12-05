package com.recorday.recorday.storage.controller;

import java.time.Duration;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recorday.recorday.auth.entity.CustomUserPrincipal;
import com.recorday.recorday.storage.dto.PresignedUploadResponse;
import com.recorday.recorday.storage.service.FileStorageService;
import com.recorday.recorday.util.response.Response;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user/files")
public class FileController {

	private final FileStorageService fileStorageService;

	@PostMapping("/presigned-upload")
	public ResponseEntity<Response<PresignedUploadResponse>> createPresignedUpload(
		@RequestParam("dir") String dir,
		@RequestParam("filename") String filename,
		@RequestParam("contentType") String contentType,
		@AuthenticationPrincipal CustomUserPrincipal principal
	) {

		PresignedUploadResponse response = fileStorageService.generatePresignedUploadUrl(
			dir,
			filename,
			contentType,
			Duration.ofMinutes(5),
			principal.getId()
		);

		return Response.ok(response).toResponseEntity();
	}

	@DeleteMapping("/delete")
	public ResponseEntity<Response<Void>> deleteFile(@RequestParam("key") String key) {

		fileStorageService.delete(key);

		return Response.ok().toResponseEntity();
	}
}
