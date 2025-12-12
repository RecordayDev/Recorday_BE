package com.recorday.recorday.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
public class SchedulingConfig {

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}
}
