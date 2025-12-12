package com.recorday.recorday.auth.oauth2.provider.naver.service;

import org.springframework.stereotype.Service;

import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.auth.oauth2.provider.naver.component.NaverDecryptUniqueId;
import com.recorday.recorday.auth.oauth2.provider.naver.dto.NaverUnlinkRequest;
import com.recorday.recorday.auth.service.UserExitService;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.util.user.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NaverOAuth2UnlinkService {

	private final NaverDecryptUniqueId decryptUniqueId;
	private final UserReader userReader;
	private final UserExitService userExitService;

	public void unlink(NaverUnlinkRequest request) throws Exception{

		String providerId = decryptUniqueId.handleUnlinkNotification(request);

		User user = userReader.getUserByProviderAndProviderId(Provider.NAVER, providerId);

		userExitService.requestExit(user.getId());
	}


}
