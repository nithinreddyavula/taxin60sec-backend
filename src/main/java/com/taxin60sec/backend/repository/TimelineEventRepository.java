package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.TimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimelineEventRepository extends JpaRepository<TimelineEvent, Long> {
}
