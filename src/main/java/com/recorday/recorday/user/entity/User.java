package com.recorday.recorday.user.entity;

import java.time.LocalDateTime;
import java.util.Random;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
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
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
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
	},
	indexes = {
		@Index(name = "idx_user_public_id", columnList = "public_id")
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

	@Column(name = "public_id", nullable = false, unique = true, length = 12)
	private String publicId;

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

	@Builder.Default
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private UserStatus userStatus = UserStatus.ACTIVE;

	private LocalDateTime deleteRequestedAt;

	@PrePersist
	public void generatePublicId() {
		if (this.publicId == null) {
			this.publicId = NanoIdUtils.randomNanoId(
				NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
				NanoIdUtils.DEFAULT_ALPHABET,
				10
			);
		}
	}

	public void deleteRequested() {
		this.userStatus = UserStatus.DELETED_REQUESTED;
		this.deleteRequestedAt = LocalDateTime.now();
	}

	public void reActivate() {
		this.userStatus = UserStatus.ACTIVE;
		this.deleteRequestedAt = null;
	}

	public void delete() {
		this.userStatus = UserStatus.DELETED;
		this.email = "deleted_" + this.id + "@recorday.local";
		this.username = "탈퇴한 사용자";
		this.password = null;
		this.profileUrl = "resources/defaults/userDefaultImage.png";
		this.providerId = null;
	}

	public boolean isReadyForDeletion(LocalDateTime now) {
		if (this.userStatus != UserStatus.DELETED_REQUESTED || this.deleteRequestedAt == null) {
			return false;
		}
		return this.deleteRequestedAt.plusDays(7).isBefore(now);
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
