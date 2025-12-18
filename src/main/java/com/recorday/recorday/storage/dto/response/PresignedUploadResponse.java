package com.recorday.recorday.storage.dto.response;

import java.time.Duration;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned URL 생성 응답 DTO")
public record PresignedUploadResponse(

	@Schema(description = "S3에 저장될 키 (경로 포함)", example = "uploads/users/publicId/profile/550e8400-e29b-41d4-a716-446655440000.png")
	String key,          // S3 object key

	@Schema(description = "파일 업로드를 수행할 Presigned URL", example = "https://recorday-bucket.s3.ap-northeast-2.amazonaws.com/...")
	String uploadUrl,    // presigned URL

	@Schema(description = "파일 타입", example = "image/png")
	String contentType,   // 타입

	@Schema(description = "URL 유효 기간", example = "PT5M")
	Duration expiresIn   // 유효 시간
) {
}
