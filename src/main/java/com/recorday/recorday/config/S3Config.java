package com.recorday.recorday.config;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.recorday.recorday.storage.service.FileStorageService;
import com.recorday.recorday.storage.service.S3FileStorageService;
import com.recorday.recorday.storage.strategy.UploadPathStrategy;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	@Value("${aws.s3.bucket-url}")
	private String bucketUrl;

	@Value("${aws.s3.region}")
	private String region;

	@Value("${aws.credentials.access-key}")
	private String accessKey;

	@Value("${aws.credentials.secret-key}")
	private String secretKey;

	@Bean
	public S3Client s3Client() {

		AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

		return S3Client.builder()
			.region(Region.of(region))
			.credentialsProvider(StaticCredentialsProvider.create(credentials))
			.build();
	}

	@Bean
	public S3Presigner s3Presigner() {

		AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

		return S3Presigner.builder()
			.region(Region.of(region))
			.credentialsProvider(StaticCredentialsProvider.create(credentials))
			.build();
	}

	@Bean
	public FileStorageService fileStorageService(
		S3Client s3Client,
		S3Presigner s3Presigner,
		Set<UploadPathStrategy> strategies
	) {
		return new S3FileStorageService(
			s3Client,
			s3Presigner,
			strategies,
			bucketName
		);
	}
}
