package com.recorday.recorday.auth.oauth2.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.recorday.recorday.auth.entity.CustomOAuth2User;
import com.recorday.recorday.auth.oauth2.service.AuthCodeService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	@Value("${redirect-url.frontend}")
	private String REDIRECT_URL;

	private final AuthCodeService authCodeService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		CustomOAuth2User principal = (CustomOAuth2User)authentication.getPrincipal();
		String publicId = principal.getPublicId();
		String provider = principal.getProvider().name();

		String authorizationCode = authCodeService.saveAuthCode(publicId, provider);

		getRedirectStrategy().sendRedirect(request, response,
			REDIRECT_URL + "/oauth2/callback?authorization_code=" + authorizationCode);
	}
}
