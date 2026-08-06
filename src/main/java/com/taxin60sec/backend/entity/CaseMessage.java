package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * A single in-app chat message on a case. This is the masked communication layer's
 * text channel: client, assigned CA, and admin can all read/write on a case they
 * have access to, and this is the ONLY channel the product supports - there is
 * deliberately no field anywhere that exposes the other party's raw phone number
 * or email to each other through this API.
 */
@Entity
@Getter
@Setter
@Table(name = "case_messages")
public class CaseMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private Case caseRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // Denormalized at send time so the message still shows who sent it (client/CA/admin)
    // even if the sender's roles change later.
    @Column(length = 20, nullable = false)
    private String senderRole;

    @NotBlank
    @Size(max = 4000)
    @Column(length = 4000, nullable = false)
    private String content;

    private Instant readAt;
}