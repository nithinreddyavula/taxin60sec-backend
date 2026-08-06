package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:module IS NULL OR a.entityType = :module) " +
            "AND (:search IS NULL OR LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(a.actorId) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLog> search(@Param("module") String module, @Param("search") String search, Pageable pageable);
}