package com.recorday.recorday.auth.jwt.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.recorday.recorday.auth.entity.CustomUserPrincipal;
import com.recorday.recorday.auth.exception.AuthErrorCode;
import com.recorday.recorday.auth.exception.CustomAuthenticationException;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.service.UserPrincipalLoader;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	private JwtTokenService jwtTokenService;

	@Mock
	private UserPrincipalLoader userPrincipalLoader;

	@Mock
	private AuthenticationEntryPoint authenticationEntryPoint;

	@Mock
	private FilterChain filterChain;

	@Mock
	private CustomUserPrincipal customUserPrincipal;

	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@BeforeEach
	void setUp() throws Exception {
		jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenService, userPrincipalLoader, authenticationEntryPoint);
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("JWT 필터 제외 경로인 경우")
	void shouldNotFilter_excludedPath() throws ServletException, IOException {
		//given
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/local/login");
		MockHttpServletResponse response = new MockHttpServletResponse();

		//when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		//then
		then(jwtTokenService).shouldHaveNoInteractions();
		then(userPrincipalLoader).shouldHaveNoInteractions();
		then(filterChain).should().doFilter(request, response);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	@DisplayName("Authorization 헤더가 없는 경우 JWT 필터 검증 없이 통과")
	void doFilter_noAuthorizationHeader() throws ServletException, IOException {
		//given
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
		MockHttpServletResponse response = new MockHttpServletResponse();

		//when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		//then
		then(jwtTokenService).shouldHaveNoInteractions();
		then(userPrincipalLoader).shouldHaveNoInteractions();
		then(filterChain).should().doFilter(request, response);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	@DisplayName("JWT 필터 유효하지 않은 토큰")
	void doFilter_invalidToken() throws ServletException, IOException {
		//given
		String invalidToken = "invalid-token";
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
		request.addHeader("Authorization", "Bearer " + invalidToken);
		MockHttpServletResponse response = new MockHttpServletResponse();

		willThrow(new CustomAuthenticationException(AuthErrorCode.INVALID_TOKEN))
			.given(jwtTokenService).validateToken(invalidToken);

		//when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		//then
		then(jwtTokenService).should().validateToken(invalidToken);
		then(jwtTokenService).should(never()).getUserPublicId(anyString());
		then(userPrincipalLoader).shouldHaveNoInteractions();

		then(filterChain).should(never()).doFilter(request, response);

		then(authenticationEntryPoint).should().commence(eq(request), eq(response), any(CustomAuthenticationException.class));

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	@DisplayName("유효한 토큰이며 기존 인증 정보 없음")
	void doFilter_validToken_setsAuthentication() throws ServletException, IOException {
		//given
		String validToken = "valid-token";
		String publicId = "public-id";
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
		request.addHeader("Authorization", "Bearer " + validToken);
		MockHttpServletResponse response = new MockHttpServletResponse();

		given(jwtTokenService.getUserPublicId(validToken)).willReturn(publicId);
		given(userPrincipalLoader.loadUserByPublicId(publicId)).willReturn(customUserPrincipal);
		given(customUserPrincipal.getAuthorities()).willReturn(Collections.emptyList());

		//when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		//then
		then(jwtTokenService).should().validateToken(validToken);
		then(jwtTokenService).should().getUserPublicId(validToken);
		then(userPrincipalLoader).should().loadUserByPublicId(publicId);
		then(filterChain).should().doFilter(request, response);

		var authentication = SecurityContextHolder.getContext().getAuthentication();
		assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
		assertThat(authentication.getPrincipal()).isEqualTo(customUserPrincipal);
		assertThat(authentication.getAuthorities()).isEmpty();
	}

	@Test
	@DisplayName("유효한 토큰이지만 기존 인증 정보 존재")
	void doFilter_existingAuthentication_doesNotOverride() throws ServletException, IOException {
		//given
		String validToken = "valid-token";
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
		request.addHeader("Authorization", "Bearer " + validToken);
		MockHttpServletResponse response = new MockHttpServletResponse();

		UsernamePasswordAuthenticationToken existingAuth =
			new UsernamePasswordAuthenticationToken("existingUser", null, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(existingAuth);


		//when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		//then
		then(jwtTokenService).should().validateToken(validToken);
		then(jwtTokenService).should(never()).getUserPublicId(anyString());
		then(userPrincipalLoader).shouldHaveNoInteractions();
		then(filterChain).should().doFilter(request, response);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existingAuth);
	}

	@Test
	@DisplayName("shouldNotFilter는 EXCLUDED_PATHS에 매칭되지 않으면 false를 반환한다")
	void shouldNotFilter_notMatched() throws ServletException {
		// given
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

		// when
		boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

		// then
		assertThat(result).isFalse();
	}
}