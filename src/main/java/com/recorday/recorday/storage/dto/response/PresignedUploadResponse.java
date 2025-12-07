package com.recorday.recorday.storage.dto.response;

import java.time.Duration;

public record PresignedUploadResponse(
	String key,          // S3 object key
	String uploadUrl,    // presigned URL
	Duration expiresIn   // 유효 시간
) {
}
