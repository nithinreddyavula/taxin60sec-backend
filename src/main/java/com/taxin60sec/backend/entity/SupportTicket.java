package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import com.taxin60sec.backend.entity.enums.SupportTicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "support_tickets")
public class SupportTicket extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raised_by_id", nullable = false)
    private User raisedBy;

    /** Optional - a ticket doesn't have to be about a specific case. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case relatedCase;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupportTicketStatus status = SupportTicketStatus.OPEN;
}