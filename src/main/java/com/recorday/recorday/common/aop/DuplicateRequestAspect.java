package com.recorday.recorday.common.aop;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.recorday.recorday.common.annotation.PreventDuplicateRequest;
import com.recorday.recorday.common.enums.LockStrategy;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.exception.GlobalErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DuplicateRequestAspect {

	private final StringRedisTemplate redisTemplate;

	private final ExpressionParser parser = new SpelExpressionParser();
	private final ParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();

	@Around("@annotation(preventDuplicateRequest)")
	public Object checkDuplicate(ProceedingJoinPoint joinPoint, PreventDuplicateRequest preventDuplicateRequest) throws
		Throwable {
		String uniqueKey = generateUniqueKey(joinPoint, preventDuplicateRequest.key());
		String redisKey = "duplicate:request:" + uniqueKey;

		long time = preventDuplicateRequest.time();
		TimeUnit unit = preventDuplicateRequest.timeUnit();
		LockStrategy strategy = preventDuplicateRequest.strategy();

		try {
			Boolean isSuccess = redisTemplate.opsForValue()
				.setIfAbsent(redisKey, "locked", Duration.ofMillis(unit.toMillis(time)));

			if (isSuccess == null || !isSuccess) {
				log.warn("중복 요청 차단됨 - Key: {}", redisKey);
				throw new BusinessException(GlobalErrorCode.DUPLICATE_REQUEST);
			}

		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw e;
			}

			if (strategy == LockStrategy.FAIL_OPEN) {
				log.error("Redis 연결 실패 - Fail Open 정책에 의해 로직 실행 허용. Error: {}", e.getMessage());
			} else {
				log.error("Redis 연결 실패 - Fail Close 정책에 의해 로직 차단. Error: {}", e.getMessage());
				throw new BusinessException(GlobalErrorCode.REDIS_CONNECTION_ERROR);
			}

		}
		return joinPoint.proceed();
	}

	private String generateUniqueKey(ProceedingJoinPoint joinPoint, String spelKey) {
		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		Method method = signature.getMethod();

		// 1. 키를 별도로 지정하지 않은 경우 -> 메서드 이름만 사용 (전역 락이 될 수 있음)
		if (!StringUtils.hasText(spelKey)) {
			return method.getName();
		}

		// 2. SpEL Context 준비
		StandardEvaluationContext context = new StandardEvaluationContext();
		String[] parameterNames = discoverer.getParameterNames(method);
		Object[] args = joinPoint.getArgs();

		// 파라미터 이름과 값을 매핑 (예: request -> EmailAuthRequest 객체)
		if (parameterNames != null) {
			for (int i = 0; i < parameterNames.length; i++) {
				context.setVariable(parameterNames[i], args[i]);
			}
		}

		// 3. SpEL 파싱 및 값 추출
		Object value = parser.parseExpression(spelKey).getValue(context);

		return method.getName() + ":" + (value != null ? value.toString() : "unknown");
	}
}
