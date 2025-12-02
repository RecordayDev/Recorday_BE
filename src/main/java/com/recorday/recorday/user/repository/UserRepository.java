package com.recorday.recorday.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.enums.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByProviderAndEmail(Provider provider, String email);

	Optional<User> findByProviderAndEmailAndDeleted(Provider provider, String email, UserStatus deleted);

	Optional<User> findByProviderAndEmail(Provider provider, String email);

	Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}
