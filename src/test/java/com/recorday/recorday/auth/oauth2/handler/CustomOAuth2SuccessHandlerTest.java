package com.recorday.recorday.auth.oauth2.handler;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.recorday.recorday.auth.entity.CustomOAuth2User;
import com.recorday.recorday.auth.oauth2.service.AuthCodeService;
import com.recorday.recorday.auth.oauth2.enums.Provider;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2SuccessHandlerTest {

	@Mock
	private AuthCodeService authCodeService;

	@Mock
	private CustomOAuth2User customOAuth2User;

	@Mock
	private Authentication authentication;

	@Test
	@DisplayName("oauth2 로그인 성공 핸들러")
	void oAuth2LoginSuccessHandler() throws Exception {
		// given
		String publicId = "publicId";
		Provider provider = Provider.KAKAO;
		String authCode = "authCode";
		String redirectBaseUrl = "http://localhost:3000";

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		given(authCodeService.saveAuthCode(publicId, provider.name())).willReturn(authCode);
		given(authentication.getPrincipal()).willReturn(customOAuth2User);
		given(customOAuth2User.getPublicId()).willReturn(publicId);
		given(customOAuth2User.getProvider()).willReturn(provider);
		CustomOAuth2SuccessHandler successHandler = new CustomOAuth2SuccessHandler(authCodeService);

		ReflectionTestUtils.setField(successHandler, "REDIRECT_URL", redirectBaseUrl);

		//when
		successHandler.onAuthenticationSuccess(request, response, authentication);

		//then
		then(authCodeService).should().saveAuthCode(publicId, provider.name());
		then(customOAuth2User).should().getPublicId();
		then(customOAuth2User).should().getProvider();

		String expectedRedirectUrl = redirectBaseUrl + "/oauth2/kakao/callback?authorization_code=" + authCode;
		assertThat(response.getRedirectedUrl()).isEqualTo(expectedRedirectUrl);
	}

}