package com.recorday.recorday.storage.service;

import java.io.InputStream;
import java.time.Duration;

import com.recorday.recorday.storage.dto.response.PresignedUploadResponse;
import com.recorday.recorday.storage.enums.UploadType;

public interface FileStorageService {

	/**
	 * @param dir          S3 상의 디렉토리 경로
	 * @param filename     원본 파일 이름 또는 우리가 만든 파일명
	 * @param inputStream  파일 데이터
	 * @param contentLength 파일 크기 (바이트)
	 * @param contentType  MIME 타입 (예: "image/jpeg")
	 * @return 업로드된 객체의 S3 key (예: "profile/uuid.jpg")
	 */
	String upload(String dir,
		String filename,
		InputStream inputStream,
		long contentLength,
		String contentType);

	/**
	 * @param key S3 object key
	 */
	void delete(String key);

	/**
	 * @param key    S3 object key
	 * @return presigned URL 문자열
	 */
	String generatePresignedUrl(String key);

	/**
	 * 클라이언트가 S3에 직접 PUT 업로드 하기 위한 Presigned URL 발급
	 */
	PresignedUploadResponse generatePresignedUploadUrl(
		UploadType uploadType,
		String originalFilename,
		String contentType,
		Long userId
	);
}
