package com.recorday.recorday.storage.service;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final String bucketName;

	@Override
	@Transactional
	public String upload(String dir, String filename, InputStream inputStream, long contentLength, String contentType) {
		String extension = extractExtension(filename);
		String uniqueName = UUID.randomUUID() + extension;
		String key = (dir != null && !dir.isBlank())
			? dir + "/" + uniqueName
			: uniqueName;

		PutObjectRequest request = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(key)
			.contentType(contentType)
			.contentLength(contentLength)
			.build();

		s3Client.putObject(
			request,
			RequestBody.fromInputStream(inputStream, contentLength)
		);

		log.info("Uploaded file to S3. bucket={}, key={}", bucketName, key);
		return key;
	}

	@Override
	@Transactional
	public void delete(String key) {
		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
			.bucket(bucketName)
			.key(key)
			.build();

		s3Client.deleteObject(deleteObjectRequest);

		log.info("Deleted file from S3. bucket={}, key={}", bucketName, key);
	}

	@Override
	public String generatePresignedUrl(String key, Duration expiry) {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(bucketName)
			.key(key)
			.build();

		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(expiry)
			.getObjectRequest(getObjectRequest)
			.build();

		return s3Presigner.presignGetObject(presignRequest).url().toString();
	}

	private String extractExtension(String filename) {
		if (filename == null) {
			return "";
		}
		int idx = filename.lastIndexOf('.');
		if (idx == -1) {
			return "";
		}
		return filename.substring(idx);
	}
}
