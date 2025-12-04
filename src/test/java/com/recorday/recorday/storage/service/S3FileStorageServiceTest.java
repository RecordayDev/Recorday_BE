package com.recorday.recorday.storage.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3FileStorageServiceTest {

	private static final String BUCKET = "recorday-bucket";
	private static final String BUCKET_URL = "https://recorday-bucket.s3.ap-northeast-2.amazonaws.com";

	@Mock
	private S3Client s3Client;

	@Mock
	private S3Presigner s3Presigner;

	private FileStorageService fileStorageService;

	@BeforeEach
	void setUp() {
		fileStorageService = new S3FileStorageService(
			s3Client,
			s3Presigner,
			BUCKET
		);
	}

	@Test
	@DisplayName("파일 업로드에 성공하면 S3 key를 반환하고, S3Client.putObject를 올바르게 호출한다")
	void uploadFile_success() {
		//given
		String dir = "profile";
		String filename = "test.png";
		byte[] bytes = "dummy-image".getBytes();
		InputStream is = new ByteArrayInputStream(bytes);

		given(s3Client.putObject(
			any(PutObjectRequest.class),
			any(RequestBody.class)
		)).willReturn(PutObjectResponse.builder().build());

		//when
		String key = fileStorageService.upload(
			dir,
			filename,
			is,
			bytes.length,
			"image/png"
		);

		//then
		assertThat(key).startsWith(dir + "/").endsWith(".png");

		ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
		then(s3Client).should().putObject(
			requestCaptor.capture(),
			any(RequestBody.class)
		);

		PutObjectRequest captured = requestCaptor.getValue();
		assertThat(captured.bucket()).isEqualTo(BUCKET);
		assertThat(captured.key()).isEqualTo(key);
		assertThat(captured.contentType()).isEqualTo("image/png");
	}

	@Test
	@DisplayName("파일 삭제 시 S3Client.deleteObject가 올바른 bucket과 key로 호출된다")
	void deleteFile_success() {
		//given
		String key = "profile/test.png";

		//when
		fileStorageService.delete(key);

		//then
		ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);

		then(s3Client).should().deleteObject(captor.capture());

		assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
		assertThat(captor.getValue().key()).isEqualTo(key);
	}

	@Test
	@DisplayName("파일 key와 만료 시간을 넘기면 presigned URL을 생성해서 반환한다")
	void generatePresignedUrl() {
		//given
		String key = "profile/test.png";
		Duration duration = Duration.ofMinutes(10);
		String expected = "https://example.com/" + key;

		SdkHttpRequest httpRequest = SdkHttpRequest.builder()
			.method(SdkHttpMethod.GET)
			.uri(URI.create(expected))
			.build();

		PresignedGetObjectRequest presignedGetObjectRequest =
			PresignedGetObjectRequest.builder()
				.httpRequest(httpRequest)
				.expiration(Instant.now().plus(duration))
				.signedHeaders(Map.of("Host", List.of("example.com")))
				.isBrowserExecutable(true)
				.build();

		given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
			.willReturn(presignedGetObjectRequest);

		//when
		String url = fileStorageService.generatePresignedUrl(key, duration);

		//then
		assertThat(url).isEqualTo(expected);

		then(s3Presigner).should().presignGetObject(any(GetObjectPresignRequest.class));
	}
}