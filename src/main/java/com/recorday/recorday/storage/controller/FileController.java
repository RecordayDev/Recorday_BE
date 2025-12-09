package com.recorday.recorday.storage.controller;

import java.time.Duration;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recorday.recorday.auth.entity.CustomUserPrincipal;
import com.recorday.recorday.storage.dto.request.PresignedUploadRequest;
import com.recorday.recorday.storage.dto.response.PresignedUploadResponse;
import com.recorday.recorday.storage.enums.UploadType;
import com.recorday.recorday.storage.service.FileStorageService;
import com.recorday.recorday.util.response.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "File Storage", description = "파일 업로드 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user/files")
public class FileController {

	private final FileStorageService fileStorageService;

	@Operation(
		summary = "Presigned URL 생성",
		description = "S3에 파일을 업로드하기 위한 Presigned URL을 생성합니다. 업로드 타입(type)에 따라 저장 경로가 결정됩니다."
	)
	@PostMapping("/presigned-upload")
	public ResponseEntity<Response<PresignedUploadResponse>> createPresignedUpload(
		@RequestBody @Valid PresignedUploadRequest request,
		@AuthenticationPrincipal CustomUserPrincipal principal
	) {

		PresignedUploadResponse response = fileStorageService.generatePresignedUploadUrl(
			request.type(),
			request.filename(),
			request.contentType(),
			principal.getId()
		);

		return Response.ok(response).toResponseEntity();
	}

	@Operation(
		summary = "S3 객체 삭제",
		description = "S3에 업로드 되어있는 파일을 key를 참고해 삭제합니다."
	)
	@DeleteMapping("/delete")
	public ResponseEntity<Response<Void>> deleteFile(@RequestParam("key") String key) {

		fileStorageService.delete(key);

		return Response.ok().toResponseEntity();
	}
}
