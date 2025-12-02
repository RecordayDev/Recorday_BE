package com.recorday.recorday.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.recorday.recorday.auth.jwt.filter.JwtAuthenticationFilter;
import com.recorday.recorday.auth.jwt.service.JwtTokenService;
import com.recorday.recorday.auth.service.UserPrincipalLoader;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private static final List<String> AUTH_EXCLUDED_PATHS = List.of(
		"/",
		"/api/recorday/login",
		"/api/recorday/register",
		"/api/oauth2/**"
	);

	private static final List<String> CORS_WHITELIST = List.of(
		"http://localhost:5173"
	);

	private final UserDetailsService userDetailsService;
	private final UserPrincipalLoader userPrincipalLoader;
	private final JwtTokenService jwtTokenService;
	private final OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService;
	private final AuthenticationSuccessHandler customOAuth2SuccessHandler;
	private final AuthenticationFailureHandler customOAuth2FailureHandler;
	// private final AuthenticationEntryPoint customAuthenticationEntryPoint;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authManagerBuilder =
			http.getSharedObject(AuthenticationManagerBuilder.class);

		authManagerBuilder
			.userDetailsService(userDetailsService)
			.passwordEncoder(passwordEncoder());

		return authManagerBuilder.build();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.cors(cors -> cors.configurationSource(corsConfigurationSource()));

		http
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
				.successHandler(customOAuth2SuccessHandler)
				.failureHandler(customOAuth2FailureHandler)
			);

		http
			.addFilterBefore(
				new JwtAuthenticationFilter(jwtTokenService, userPrincipalLoader),
				UsernamePasswordAuthenticationFilter.class
			);

		// http
		// 	.exceptionHandling(ex -> ex
		// 		.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
		// 	);

		// http
		// 	.exceptionHandling(ex -> ex
		// 		.authenticationEntryPoint(customAuthenticationEntryPoint)
		// 	);

		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(AUTH_EXCLUDED_PATHS.toArray(String[]::new)).permitAll()
				.requestMatchers("/api/auth/guest/**").hasRole("GUEST")
				.requestMatchers("/api/auth/user/**").hasRole("USER")
				.anyRequest().authenticated()
			);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		// 프론트엔드 Origin 허용
		config.setAllowedOrigins(CORS_WHITELIST);

		// 허용할 HTTP 메서드
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

		// 허용할 헤더
		config.setAllowedHeaders(List.of("*"));

		// 인증 정보(쿠키, Authorization 헤더 등) 포함 허용
		config.setAllowCredentials(true);

		// 클라이언트에서 읽을 수 있는 헤더 (옵션)
		config.setExposedHeaders(List.of("Authorization"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

}
