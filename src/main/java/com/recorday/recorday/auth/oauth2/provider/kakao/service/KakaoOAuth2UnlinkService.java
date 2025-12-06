package com.recorday.recorday.auth.oauth2.provider.kakao.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.auth.oauth2.provider.kakao.adaptor.KakaoAuthProperties;
import com.recorday.recorday.auth.oauth2.service.OAuth2UnlinkService;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuth2UnlinkService implements OAuth2UnlinkService {

	private final RestTemplate restTemplate;
	private final KakaoAuthProperties kakaoProperties;

	@Override
	public boolean supports(Provider provider) {
		return provider == Provider.KAKAO;
	}

	@Override
	public void unlink(User user) {

		if (user.getProvider() != Provider.KAKAO) {
			return;
		}

		// Header 세팅
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "KakaoAK " + kakaoProperties.getAdminKey());
		headers.set("Content-Type", kakaoProperties.getUnlinkContentType());

		// Body 세팅 (폼 파라미터)
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("target_id_type", kakaoProperties.getUnlinkTargetIdType());
		body.add("target_id", user.getProviderId());

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

		user.unlinkProvider();

		try {
			restTemplate.postForEntity(
				kakaoProperties.getUnlinkUrl(),
				request,
				String.class
			);
		} catch (Exception e) {
			throw new BusinessException(AuthErrorCode.OAUTH2_PROVIDER_UNLINK_ERROR);
		}

	}
}
