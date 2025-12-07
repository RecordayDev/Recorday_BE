package com.recorday.recorday.storage.dto.request;

import com.recorday.recorday.storage.enums.UploadType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Presigned URL 생성 요청 DTO")
public record PresignedUploadRequest(

	@NotNull(message = "업로드 타입은 필수입니다.")
	@Schema(description = "업로드 타입 (PROFILE, FRAME 등)", example = "PROFILE")
	UploadType type,

	@NotBlank(message = "파일명은 필수입니다.")
	@Schema(description = "원본 파일명 (확장자 포함)", example = "profile_image.png")
	String filename,

	@NotBlank(message = "컨텐츠 타입은 필수입니다.")
	@Schema(description = "파일의 Content-Type", example = "image/png")
	String contentType
) {
}
