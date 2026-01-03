package com.recorday.recorday.auth.local.service;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.auth.entity.CustomUserPrincipal;
import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.exception.CustomAuthenticationException;
import com.recorday.recorday.auth.jwt.dto.AuthTokenCookies;
import com.recorday.recorday.auth.jwt.dto.TokenResponse;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.jwt.service.RefreshTokenService;
import com.recorday.recorday.auth.local.dto.request.LocalLoginRequest;
import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;
import com.recorday.recorday.auth.local.dto.response.LoginResult;
import com.recorday.recorday.auth.utils.CookieUtil;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.util.user.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalSecurityLoginServiceImpl implements LocalLoginService {

	private final UserReader userReader;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenService jwtTokenService;
	private final RefreshTokenService refreshTokenService;
	private final CookieUtil cookieUtil;

	@Override
	@Transactional
	public LoginResult login(LocalLoginRequest request) {
		try {
			Authentication authenticate = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email(), request.password())
			);

			CustomUserPrincipal principal = (CustomUserPrincipal)authenticate.getPrincipal();
			String publicId = principal.getPublicId();

			User user = userReader.getUserByPublicId(publicId);

			String accessToken = jwtTokenService.createAccessToken(publicId);
			String refreshToken = jwtTokenService.createRefreshToken(publicId);

			refreshTokenService.saveRefreshToken(publicId, refreshToken);

			ResponseCookie accessCookie = cookieUtil.createTokenCookie("accessToken", accessToken, jwtTokenService.getAccessTokenValidityMillis());
			ResponseCookie refreshCookie = cookieUtil.createTokenCookie("refreshToken", refreshToken, jwtTokenService.getRefreshTokenValidityMillis());

			AuthTokenCookies cookies = new AuthTokenCookies(accessCookie, refreshCookie);

			return new LoginResult(cookies, user.getUserStatus());
		} catch (BadCredentialsException e) {
			throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
		} catch (AuthenticationException e) {
			if (e.getCause() instanceof CustomAuthenticationException custom) {
				throw new BusinessException(custom.getErrorCode());
			}
			throw new BusinessException(AuthErrorCode.AUTHENTICATION_FAILED);

		}
	}
}
