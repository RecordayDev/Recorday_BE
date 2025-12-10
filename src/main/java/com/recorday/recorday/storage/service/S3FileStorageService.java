package com.recorday.recorday.storage.service;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.storage.dto.response.PresignedUploadResponse;
import com.recorday.recorday.storage.enums.UploadType;
import com.recorday.recorday.storage.exception.StorageErrorCode;
import com.recorday.recorday.storage.strategy.UploadPathStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final String bucketName;
	private final Map<UploadType, UploadPathStrategy> strategyMap;

	private static final Duration EXPIRY = Duration.ofDays(1);

	public S3FileStorageService(
		S3Client s3Client,
		S3Presigner s3Presigner,
		Set<UploadPathStrategy> strategies,
		String bucketName
	) {
		this.s3Client = s3Client;
		this.s3Presigner = s3Presigner;
		this.bucketName = bucketName;
		this.strategyMap = strategies.stream()
			.collect(Collectors.toMap(UploadPathStrategy::getUploadType, Function.identity()));
	}

	@Deprecated
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
	public String generatePresignedUrl(String key) {

		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(bucketName)
			.key(key)
			.build();

		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(EXPIRY)
			.getObjectRequest(getObjectRequest)
			.build();

		return s3Presigner.presignGetObject(presignRequest).url().toString();
	}

	@Override
	@Transactional
	public PresignedUploadResponse generatePresignedUploadUrl(
		UploadType uploadType,
		String originalFilename,
		String contentType,
		String publicId,
		boolean isTemp
	) {

		UploadPathStrategy strategy = strategyMap.get(uploadType);
		if (strategy == null) {
			throw new BusinessException(StorageErrorCode.UNSUPPORTED_UPLOAD_TYPE,
				StorageErrorCode.UNSUPPORTED_UPLOAD_TYPE.getMessage() + ": " + uploadType.name());
		}

		String key = strategy.generateKey(publicId, originalFilename, isTemp);

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(key)
			.contentType(contentType)
			.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(EXPIRY)
			.putObjectRequest(putObjectRequest)
			.build();

		PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
		String url = presigned.url().toString();

		log.info("Generated presigned upload URL. bucket={}, key={}", bucketName, key);

		return new PresignedUploadResponse(key, url, EXPIRY);
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
