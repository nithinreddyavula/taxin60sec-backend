package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.CAProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CAProfileRepository extends JpaRepository<CAProfile, Long> {
    Optional<CAProfile> findByUserId(Long userId);
    List<CAProfile> findByVerifiedFalseOrderByCreatedAtAsc();
    List<CAProfile> findByVerifiedTrueOrderByCreatedAtAsc();
}