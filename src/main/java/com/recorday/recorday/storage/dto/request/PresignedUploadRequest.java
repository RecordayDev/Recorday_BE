package com.recorday.recorday.storage.dto.request;

import com.recorday.recorday.storage.enums.UploadType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresignedUploadRequest(
	@NotNull(message = "업로드 타입은 필수입니다.")
	UploadType type,

	@NotBlank(message = "파일명은 필수입니다.")
	String filename,

	@NotBlank(message = "컨텐츠 타입은 필수입니다.")
	String contentType
) {
}
