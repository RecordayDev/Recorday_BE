package com.recorday.recorday.auth.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.enums.UserRole;
import com.recorday.recorday.user.enums.UserStatus;

import lombok.Getter;

@Getter
public class CustomUserPrincipal implements UserDetails {

	private final Long id;
	private final String publicId;
	private final String email;
	private final String password;
	private final UserRole userRole;
	private final UserStatus status;

	public CustomUserPrincipal(User user) {
		this.id = user.getId();
		this.publicId = user.getPublicId();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.userRole = user.getUserRole();
		this.status = user.getUserStatus();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		Collection<GrantedAuthority> authorities = new ArrayList<>();

		if (status == UserStatus.DELETED_REQUESTED) {
			authorities.add(new SimpleGrantedAuthority("ROLE_DELETED_REQUESTED"));
		} else if (status == UserStatus.ACTIVE) {
			authorities.add(new SimpleGrantedAuthority(userRole.name()));
		}

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
