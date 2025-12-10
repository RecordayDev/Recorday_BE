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
	public Object checkDuplicate(ProceedingJoinPoint joinPoint, PreventDuplicateRequest preventDuplicateRequest) throws Throwable {
		String uniqueKey = generateUniqueKey(joinPoint, preventDuplicateRequest.key());
		String redisKey = "duplicate:request:" + uniqueKey;

		long time = preventDuplicateRequest.time();
		TimeUnit unit = preventDuplicateRequest.timeUnit();

		log.debug("중복 요청 체크 시작 - Key: {}, Time: {} {}", redisKey, time, unit);

		Boolean isSuccess = redisTemplate.opsForValue()
			.setIfAbsent(redisKey, "locked", Duration.ofMillis(unit.toMillis(time)));

		if (isSuccess == null || !isSuccess) {
			log.warn("중복 요청이 차단되었습니다. Key: {}", redisKey);
			throw new BusinessException(GlobalErrorCode.DUPLICATE_REQUEST);
		}

		try {
			return joinPoint.proceed();
		} catch (Exception e) {
			throw e;
		}
	}

	private String generateUniqueKey(ProceedingJoinPoint joinPoint, String spelKey) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
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
