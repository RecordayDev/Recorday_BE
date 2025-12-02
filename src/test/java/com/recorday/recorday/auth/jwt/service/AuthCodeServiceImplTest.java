package com.recorday.recorday.auth.jwt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.jwt.dto.AuthCodePayload;
import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.auth.oauth2.service.AuthCodeServiceImpl;
import com.recorday.recorday.exception.BusinessException;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuthCodeServiceImplTest {

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	private ObjectMapper objectMapper;

	private AuthCodeServiceImpl authCodeService;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		authCodeService = new AuthCodeServiceImpl(stringRedisTemplate, objectMapper);
	}

	@Test
	@DisplayName("saveAuthCode는 authCode를 생성하고 Redis에 payload를 저장한다")
	void createAuthCode() throws Exception {
		//given
		Long userId = 1L;
		String provider = Provider.KAKAO.name();

		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

		//when
		String authCode = authCodeService.saveAuthCode(userId, provider);

		//then
		assertNotNull(authCode);
		assertFalse(authCode.isBlank());

		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);

		verify(valueOperations).set(
			keyCaptor.capture(),
			valueCaptor.capture(),
			ttlCaptor.capture(),
			eq(TimeUnit.SECONDS)
		);

		String savedKey = keyCaptor.getValue();
		String savedJson = valueCaptor.getValue();
		Long ttl = ttlCaptor.getValue();

		assertTrue(savedKey.startsWith("AUTH_CODE:"));
		assertEquals(authCode, savedKey.substring("AUTH_CODE:".length()));
		assertEquals(300L, ttl);

		AuthCodePayload payload = objectMapper.readValue(savedJson, AuthCodePayload.class);
		assertEquals(userId, payload.userId());
		assertEquals(provider, payload.provider());
	}

	@Test
	@DisplayName("getAuthPayload가 Redis에 저장된 값을 역직렬화하여 반환")
	void getAuthPayload_success() throws Exception {
		//given
		String code = "test-code";
		String key = "AUTH_CODE:" + code;
		AuthCodePayload payload = new AuthCodePayload(1L, "KAKAO");
		String json = objectMapper.writeValueAsString(payload);

		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(key)).willReturn(json);


		//when
		AuthCodePayload result = authCodeService.getAuthPayload(code);

		//then
		assertNotNull(result);
		assertEquals(1L, result.userId());
		assertEquals("KAKAO", result.provider());
	}

	@Test
	@DisplayName("getAuthPayload가 Redis에 값이 없으면 BusinessException을 던짐")
	void getAuthPayload_notFound() {
		// given
		String code = "not-exist";
		String key = "AUTH_CODE:" + code;

		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(key)).willReturn(null);

		// when, then
		BusinessException ex = assertThrows(BusinessException.class,
			() -> authCodeService.getAuthPayload(code));

		assertEquals(AuthErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());
	}

	@Test
	@DisplayName("deleteAuthCode는 Redis에서 해당 키를 삭제")
	void deleteAuthCode_success() {
		// given
		String code = "to-delete";
		String key = "AUTH_CODE:" + code;

		// when
		authCodeService.deleteAuthCode(code);

		// then
		verify(stringRedisTemplate).delete(key);
	}

}