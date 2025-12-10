package com.recorday.recorday.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.recorday.recorday.auth.jwt.property.JwtProperties;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.jwt.service.JwtTokenServiceImpl;

@Configuration
public class JwtConfig {

	@Bean
	@ConfigurationProperties(prefix = "jwt")
	public JwtProperties jwtProperties() {
		return new JwtProperties();
	}

	@Bean
	public JwtTokenService jwtTokenService(JwtProperties properties) {
		return new JwtTokenServiceImpl(
			properties.getSecret(),
			properties.getAccessExpiration().toMillis(),
			properties.getRefreshExpiration().toMillis()
		);
	}
}
