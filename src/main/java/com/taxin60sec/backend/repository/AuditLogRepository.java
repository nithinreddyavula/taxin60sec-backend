package com.taxin60sec.backend.repository;
import com.taxin60sec.backend.entity.AuditLog; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface AuditLogRepository extends JpaRepository<AuditLog,Long>{List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType,String entityId);}
