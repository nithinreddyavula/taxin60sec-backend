package com.taxin60sec.backend.entity;
import com.taxin60sec.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Entity @Getter @Setter @Table(name="audit_logs",indexes={@Index(name="idx_audit_entity_created",columnList="entity_type,entity_id,created_at"),@Index(name="idx_audit_actor_created",columnList="actor_id,created_at")})
public class AuditLog extends BaseEntity { @Column(nullable=false,length=120) private String action; @Column(nullable=false,length=100) private String entityType; @Column(length=100) private String entityId; @Column(length=100) private String actorId; @Column(length=6000) private String attributes; }
