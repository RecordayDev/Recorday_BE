package com.recorday.recorday.auth.jwt.service;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.exception.CustomAuthenticationException;
import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtTokenServiceImpl implements JwtTokenService{

	private final Key key;
	private final long accessTokenValidityMillis;
	private final long refreshTokenValidityMillis;

	public JwtTokenServiceImpl(
		String secretKey,
		long accessTokenValidityMillis,
		long refreshTokenValidityMillis) {

		this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
		this.accessTokenValidityMillis = accessTokenValidityMillis;
		this.refreshTokenValidityMillis = refreshTokenValidityMillis;
	}

	@Override
	public String createAccessToken(Long userId) {
		return createToken(userId, accessTokenValidityMillis, "ACCESS");
	}

	@Override
	public String createRefreshToken(Long userId) {
		return createToken(userId, refreshTokenValidityMillis, "REFRESH");
	}

	private String createToken(Long userId, long validityMillis, String type) {
		Instant now = Instant.now();
		Date issuedAt = Date.from(now);
		Date expiry = Date.from(now.plusMillis(validityMillis));

		return Jwts.builder()
			.setSubject(String.valueOf(userId))
			.setIssuedAt(issuedAt)
			.setExpiration(expiry)
			.setIssuer("Recorday")
			.claim("type", type)
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();
	}

	@Override
	public boolean validateToken(String token) {
		try {
			Jws<Claims> claims = Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token);

			// 만료 검사
			if (claims.getBody().getExpiration().before(new Date())) {
				throw new CustomAuthenticationException(AuthErrorCode.EXPIRED_ACCESS_TOKEN);
			}

		} catch (ExpiredJwtException e) {
			throw new CustomAuthenticationException(AuthErrorCode.EXPIRED_ACCESS_TOKEN);
		} catch (JwtException | IllegalArgumentException e) {
			throw new CustomAuthenticationException(AuthErrorCode.INVALID_ACCESS_TOKEN);
		}
		return true;
	}

	@Override
	public Long getUserId(String token) {
		Jws<Claims> claims = Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token);

		String subject = claims.getBody().getSubject();
		return Long.parseLong(subject);
	}

	@Override
	public String getTokenType(String token) {
		Jws<Claims> claims = Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token);

		return claims.getBody().get("type", String.class);
	}

	@Override
	public long getRefreshTokenValidityMillis() {
		return refreshTokenValidityMillis;
	}
}
