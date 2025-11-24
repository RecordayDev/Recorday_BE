package com.recorday.recorday.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recorday.recorday.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
