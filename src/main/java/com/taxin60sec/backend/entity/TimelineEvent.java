package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "timeline_events", indexes = {
        @Index(name = "idx_timeline_case_created", columnList = "case_id,created_at"),
        @Index(name = "idx_timeline_actor_created", columnList = "actor_user_id,created_at")
})
public class TimelineEvent extends BaseEntity {
    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String eventType;

    @NotBlank
    @Size(max = 300)
    @Column(nullable = false, length = 300)
    private String title;

    @Size(max = 1200)
    @Column(length = 1200)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case taxCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actor;
}
