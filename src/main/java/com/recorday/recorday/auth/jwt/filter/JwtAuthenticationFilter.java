package com.recorday.recorday.auth.jwt.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.recorday.recorday.auth.entity.CustomUserPrincipal;
import com.recorday.recorday.auth.exception.CustomAuthenticationException;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.service.UserPrincipalLoader;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final List<String> EXCLUDED_PATHS = List.of(
		"/",
		"/swagger-ui/**",
		"/v3/api-docs/**",
		"/login/**",
		"/api/recorday/login",
		"/api/recorday/register",
		"/api/recorday/reissue",
		"/api/oauth2/**"
	);

	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private final JwtTokenService jwtTokenService;
	private final UserPrincipalLoader userPrincipalLoader;
	private final AuthenticationEntryPoint authenticationEntryPoint;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		log.info("JWT 필터 접근 URI: {}", request.getRequestURI());

		String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authorizationHeader.substring(7);

		try {
			jwtTokenService.validateToken(token);

			if (SecurityContextHolder.getContext().getAuthentication() == null) {

				String publicId = jwtTokenService.getUserPublicId(token);
				CustomUserPrincipal principal = userPrincipalLoader.loadUserByPublicId(publicId);

				UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(
						principal,
						null,
						principal.getAuthorities()
					);

				authentication.setDetails(
					new WebAuthenticationDetailsSource().buildDetails(request)
				);

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}

		} catch (CustomAuthenticationException ex) {
			// 검증 실패(만료, 위조 등) or 사용자 조회 실패 시 공통 처리
			log.error("JWT 인증 실패: {}", ex.getMessage());
			SecurityContextHolder.clearContext();
			authenticationEntryPoint.commence(request, response, ex);
			return;
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();

		return EXCLUDED_PATHS.stream()
			.anyMatch(pattern -> pathMatcher.match(pattern, path));
	}
}
