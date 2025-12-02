package com.recorday.recorday.auth.oauth2.provider.kakao.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.recorday.recorday.auth.oauth2.provider.kakao.adaptor.KakaoAuthProperties;

@Configuration
@EnableConfigurationProperties(KakaoAuthProperties.class)
public class KakaoConfig {
}
