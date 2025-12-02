package com.recorday.recorday.auth.oauth2.provider.kakao.adaptor;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "kakao.auth")
@Getter
@Setter
public class KakaoAuthProperties {
	private String adminKey;
	private String unlinkUrl;
	private String unlinkContentType;
	private String unlinkTargetIdType;

}
