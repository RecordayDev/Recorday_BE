package com.recorday.recorday.storage.service;

import java.io.InputStream;
import java.time.Duration;

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
	 * @param expiry presigned URL 유효 시간
	 * @return presigned URL 문자열
	 */
	String generatePresignedUrl(String key, Duration expiry);
}
