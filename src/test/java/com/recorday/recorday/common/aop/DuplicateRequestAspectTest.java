package com.recorday.recorday.common.aop;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.recorday.recorday.common.annotation.PreventDuplicateRequest;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.exception.GlobalErrorCode;

@ExtendWith(MockitoExtension.class)
class DuplicateRequestAspectTest {

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
	private ProceedingJoinPoint joinPoint;

	@Mock
	private MethodSignature signature;

	@InjectMocks
	private DuplicateRequestAspect aspect;

	// 테스트용 어노테이션 Mock
	private PreventDuplicateRequest annotation;

	@BeforeEach
	void setUp() {
		// RedisTemplate이 valueOperations를 반환하도록 설정
		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		// JoinPoint가 Signature를 반환하도록 설정
		given(joinPoint.getSignature()).willReturn(signature);

		// 어노테이션 Mock 생성
		annotation = mock(PreventDuplicateRequest.class);
	}

	@Test
	@DisplayName("[성공] Redis에 키가 없으면 락을 걸고 메서드를 실행한다")
	void checkDuplicate_success_test() throws Throwable {
		// given
		String email = "test@recorday.com";
		TestRequestDto requestDto = new TestRequestDto(email);

		// Reflection 설정을 위한 더미 메서드 가져오기
		Method method = TestController.class.getMethod("testMethod", TestRequestDto.class);

		// Mock 동작 정의
		given(signature.getMethod()).willReturn(method); // 메서드 정보
		given(joinPoint.getArgs()).willReturn(new Object[] {requestDto}); // 파라미터 값

		// 어노테이션 설정
		given(annotation.key()).willReturn("#request.email"); // SpEL
		given(annotation.time()).willReturn(3000L);
		given(annotation.timeUnit()).willReturn(TimeUnit.MILLISECONDS);

		// Redis: setIfAbsent가 true 반환 (락 획득 성공)
		String expectedKey = "duplicate:request:testMethod:" + email;
		given(valueOperations.setIfAbsent(eq(expectedKey), eq("locked"), any(Duration.class)))
			.willReturn(true);

		// when
		aspect.checkDuplicate(joinPoint, annotation);

		// then
		then(joinPoint).should().proceed();
	}

	@Test
	@DisplayName("[실패] Redis에 이미 키가 있으면(중복 요청) 예외를 발생시킨다")
	void checkDuplicate_fail_duplicate_test() throws NoSuchMethodException {
		// given
		String email = "test@recorday.com";
		TestRequestDto requestDto = new TestRequestDto(email);
		Method method = TestController.class.getMethod("testMethod", TestRequestDto.class);

		given(signature.getMethod()).willReturn(method);
		given(joinPoint.getArgs()).willReturn(new Object[] {requestDto});

		given(annotation.key()).willReturn("#request.email");
		given(annotation.time()).willReturn(3000L);
		given(annotation.timeUnit()).willReturn(TimeUnit.MILLISECONDS);

		// Redis: setIfAbsent가 false 반환 (이미 키가 존재함)
		String expectedKey = "duplicate:request:testMethod:" + email;
		given(valueOperations.setIfAbsent(eq(expectedKey), eq("locked"), any(Duration.class)))
			.willReturn(false);

		// when & then
		assertThatThrownBy(() -> aspect.checkDuplicate(joinPoint, annotation))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.DUPLICATE_REQUEST);

		// proceed가 호출되지 않아야 함
		try {
			then(joinPoint).should(never()).proceed();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("[예외] SpEL 키가 없으면 메서드 이름만으로 키를 생성한다")
	void checkDuplicate_no_spel_key_test() throws Throwable {
		// given
		Method method = TestController.class.getMethod("noArgMethod");

		given(signature.getMethod()).willReturn(method);
		// Args가 필요 없음

		given(annotation.key()).willReturn(""); // 키 없음
		given(annotation.time()).willReturn(3000L);
		given(annotation.timeUnit()).willReturn(TimeUnit.MILLISECONDS);

		given(valueOperations.setIfAbsent(any(), any(), any())).willReturn(true);

		// when
		aspect.checkDuplicate(joinPoint, annotation);

		// then
		// 키가 "duplicate:request:noArgMethod" 형식이여야 함
		then(valueOperations).should().setIfAbsent(
			eq("duplicate:request:noArgMethod"),
			eq("locked"),
			any(Duration.class)
		);
		then(joinPoint).should().proceed();
	}

	static class TestRequestDto {
		private String email;

		public TestRequestDto(String email) {
			this.email = email;
		}

		public String getEmail() {
			return email;
		}
	}

	// 테스트용 타겟 클래스 (메서드 정보를 얻기 위함)
	static class TestController {
		// 파라미터 이름을 SpEL에서 읽으려면 실제 메서드 시그니처가 필요함
		// request 라는 파라미터 이름을 사용
		public void testMethod(TestRequestDto request) {
		}

		public void noArgMethod() {
		}
	}
}