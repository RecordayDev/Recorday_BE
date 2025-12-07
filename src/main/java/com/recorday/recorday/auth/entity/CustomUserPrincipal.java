package com.recorday.recorday.auth.entity;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.recorday.recorday.user.entity.User;

import lombok.Getter;

@Getter
public class CustomUserPrincipal implements UserDetails {

	private final Long id;
	private final String publicId;
	private final String email;
	private final String password;
	private final Collection<? extends GrantedAuthority> authorities;

	public CustomUserPrincipal(User user) {
		this.id = user.getId();
		this.publicId = user.getPublicId();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.authorities = List.of(
			new SimpleGrantedAuthority(user.getUserRole().name())
		);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public @Nullable String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}
}
