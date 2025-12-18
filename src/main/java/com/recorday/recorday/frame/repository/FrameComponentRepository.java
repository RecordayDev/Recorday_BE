package com.recorday.recorday.frame.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recorday.recorday.frame.entity.FrameComponent;

@Repository
public interface FrameComponentRepository extends JpaRepository<FrameComponent, Long> {
}
