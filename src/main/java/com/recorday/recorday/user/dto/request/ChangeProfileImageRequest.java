package com.recorday.recorday.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChangeProfileImageRequest(
	@Schema(description = "S3에 업로드된 프로필 이미지 파일의 Key", example = "profiles/user_123.jpg")
	String s3Key
) {
}