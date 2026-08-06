package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.RepatriationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepatriationRecordRepository extends JpaRepository<RepatriationRecord, Long> {

    List<RepatriationRecord> findByClientIdAndDeletedFalseOrderByTransactionDateDesc(Long clientId);
}