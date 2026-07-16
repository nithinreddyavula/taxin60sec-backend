package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.TimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TimelineEventRepository extends JpaRepository<TimelineEvent, Long> {
    List<TimelineEvent> findByTaxCaseIdAndDeletedFalseOrderByCreatedAtAsc(Long caseId);
}
