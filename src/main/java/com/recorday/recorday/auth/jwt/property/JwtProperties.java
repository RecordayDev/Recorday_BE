package com.recorday.recorday.auth.jwt.property;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtProperties {
	private String secret;
	private Duration accessExpiration;
	private Duration refreshExpiration;
}
