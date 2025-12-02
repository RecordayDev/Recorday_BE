package com.recorday.recorday.user.entity;

import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.user.enums.UserRole;
import com.recorday.recorday.user.enums.UserStatus;
import com.recorday.recorday.util.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "users",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_provider_email",
			columnNames = {"provider", "email"}
		),
		@UniqueConstraint(
			name = "uk_provider_provider_id",
			columnNames = {"provider", "provider_id"}
		)
	}
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Provider provider;

	@Column(name = "provider_id", length = 64)
	private String providerId;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private UserRole userRole;

	@Column(nullable = false)
	private String email;

	private String password;

	@Column(nullable = false)
	private String username;

	@Column(nullable = false, length = 1024)
	private String profileUrl;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private UserStatus deleted = UserStatus.ACTIVE;

	public void delete() {
		this.deleted = UserStatus.DELETED;
		this.email = "deleted_" + this.id + "@recorday.local";
		this.username = "탈퇴한 사용자";
		this.password = null;
		this.profileUrl = "/static/images/userDefaultImage.png";
	}

	public void changePassword(String password) {
		this.password = password;
	}

	public void changeUsername(String username) {
		this.username = username;
	}

	public void changeProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}
}
