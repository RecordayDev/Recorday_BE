package com.recorday.recorday.auth.jwt.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.exception.CustomAuthenticationException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceImplTest {

	private static final String TEST_SECRET = "this-is-a-test-secret-key-for-jwt-256";
	private static final long ACCESS_VALIDITY = Duration.ofMinutes(1).toMillis();
	private static final long REFRESH_VALIDITY = Duration.ofDays(1).toMillis();

	private JwtTokenServiceImpl jwtTokenService;
	private Key key;

	@BeforeEach
	void setUp() throws Exception {
		jwtTokenService = new JwtTokenServiceImpl(TEST_SECRET, ACCESS_VALIDITY, REFRESH_VALIDITY);
		key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	@DisplayName("액세스 토큰 생성")
	void createAccessToken_containClaims() {
		//given
		Long userId = 1L;

		//when
		String token = jwtTokenService.createAccessToken(userId);

		//then
		Jws<Claims> parsed = Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token);

		Claims claims = parsed.getBody();

		assertThat(claims.getSubject()).isEqualTo(String.valueOf(userId));
		assertThat(claims.getIssuer()).isEqualTo("Recorday");
		assertThat(claims.get("type", String.class)).isEqualTo("ACCESS");
		assertThat(claims.getExpiration()).isAfter(new Date());
	}

	@Test
	@DisplayName("리프레시 토큰 생성")
	void createRefreshToken_containClaims() {
		//given
		Long userId = 1L;

		//when
		String token = jwtTokenService.createRefreshToken(userId);

		//then
		Jws<Claims> parsed = Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token);

		Claims claims = parsed.getBody();

		assertThat(claims.getSubject()).isEqualTo(String.valueOf(userId));
		assertThat(claims.getIssuer()).isEqualTo("Recorday");
		assertThat(claims.get("type", String.class)).isEqualTo("REFRESH");
		assertThat(claims.getExpiration()).isAfter(new Date());
	}

	@Test
	@DisplayName("정상 토큰 검증 성공")
	void validateToken_validToken() {
		//given
		String token = jwtTokenService.createAccessToken(1L);

		//when & then
		assertDoesNotThrow(() -> jwtTokenService.validateToken(token));
	}

	@Test
	@DisplayName("만료 토큰 검증")
	void validateToken_expiredToken() {
		//given
		Instant now = Instant.now();
		Date past = Date.from(now.minusSeconds(60));

		String expiredToken = Jwts.builder()
			.setSubject("1")
			.setIssuedAt(Date.from(now.minusSeconds(120)))
			.setExpiration(past)
			.claim("type", "ACCESS")
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();

		//when & then
		assertThatThrownBy(() -> jwtTokenService.validateToken(expiredToken))
			.isInstanceOf(CustomAuthenticationException.class)
			.hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.EXPIRED_TOKEN);
	}

	@Test
	@DisplayName("서명이 다른 토큰 검증")
	void validateToken_invalidSignature() {
		//given
		Key anotherKey = Keys.hmacShaKeyFor("another-secret-key-for-jwt-256-!!!!".getBytes(StandardCharsets.UTF_8));

		String tokenWithDifferentKey = Jwts.builder()
			.setSubject("1")
			.setExpiration(Date.from(Instant.now().plusSeconds(600)))
			.signWith(anotherKey, SignatureAlgorithm.HS256)
			.compact();

		//when & then
		assertThatThrownBy(() -> jwtTokenService.validateToken(tokenWithDifferentKey))
			.isInstanceOf(CustomAuthenticationException.class)
			.hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_TOKEN);
	}

}