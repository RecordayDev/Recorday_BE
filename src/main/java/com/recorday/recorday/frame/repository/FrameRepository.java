package com.recorday.recorday.frame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recorday.recorday.frame.entity.Frame;
import com.recorday.recorday.user.entity.User;

@Repository
public interface FrameRepository extends JpaRepository<Frame, Long> {

	@Modifying(clearAutomatically = true)
	@Query("""
		DELETE FROM FrameComponent fc
		WHERE fc.frame.id
		IN (SELECT f.id FROM Frame f WHERE f.user.id = :userId)
	""")
	void deleteComponentsByUserId(@Param("userId") Long userId);

	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM Frame f WHERE f.user.id = :userId")
	void deleteFramesByUserId(@Param("userId")Long userId);

	List<Frame> findAllByUser(User user);
}
