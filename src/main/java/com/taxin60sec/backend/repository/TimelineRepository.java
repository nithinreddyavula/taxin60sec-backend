package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.Timeline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimelineRepository extends JpaRepository<Timeline, Long> {
}
