package com.recorday.recorday.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.recorday.recorday.storage.service.FileStorageService;
import com.recorday.recorday.storage.service.S3FileStorageService;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
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

	@Bean
	public S3Client s3Client() {
		return S3Client.builder()
			.region(Region.of(region))
			.credentialsProvider(DefaultCredentialsProvider.create())
			.build();
	}

	@Bean
	public S3Presigner s3Presigner() {
		return S3Presigner.builder()
			.region(Region.of(region))
			.credentialsProvider(DefaultCredentialsProvider.create())
			.build();
	}

	@Bean
	public FileStorageService fileStorageService(S3Client s3Client, S3Presigner s3Presigner) {
		return new S3FileStorageService(
			s3Client,
			s3Presigner,
			bucketName
		);
	}
}
