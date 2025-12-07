package com.recorday.recorday.auth.jwt.service;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.exception.CustomAuthenticationException;

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
	public String createAccessToken(String publicId) {
		return createToken(publicId, accessTokenValidityMillis, "ACCESS");
	}

	@Override
	public String createRefreshToken(String publicId) {
		return createToken(publicId, refreshTokenValidityMillis, "REFRESH");
	}

	private String createToken(String publicId, long validityMillis, String type) {
		Instant now = Instant.now();
		Date issuedAt = Date.from(now);
		Date expiry = Date.from(now.plusMillis(validityMillis));

		return Jwts.builder()
			.setSubject(publicId)
			.setIssuedAt(issuedAt)
			.setExpiration(expiry)
			.setIssuer("Recorday")
			.claim("type", type)
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();
	}

	@Override
	public void validateToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token);

		} catch (ExpiredJwtException e) {
			throw new CustomAuthenticationException(AuthErrorCode.EXPIRED_TOKEN);
		} catch (JwtException | IllegalArgumentException e) {
			throw new CustomAuthenticationException(AuthErrorCode.INVALID_TOKEN);
		}
	}

	@Override
	public String getUserPublicId(String token) {
		Jws<Claims> claims = Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token);

		return claims.getBody().getSubject();
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
