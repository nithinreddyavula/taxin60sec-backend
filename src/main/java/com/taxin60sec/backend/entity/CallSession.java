package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import com.taxin60sec.backend.entity.enums.CallStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Records a masked-number call request between a client and their assigned CA on a
 * case - the calling equivalent of CaseMessage. IMPORTANT: this entity only tracks
 * the *record* of a call (who requested it, when, status, duration). Actually
 * bridging two real phone numbers through a masked/relay number requires a
 * telephony provider (e.g. Exotel, Ozonetel, Twilio) - none is wired up yet since
 * none was specified. See CallService for exactly where that integration plugs in.
 */
@Entity
@Getter
@Setter
@Table(name = "call_sessions")
public class CallSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private Case caseRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private CallStatus status = CallStatus.REQUESTED;

    // Populated once a real telephony provider is integrated - the relay/virtual
    // number both parties are told to use for this session. Null while unwired.
    @Column(length = 30)
    private String maskedNumber;

    // Which provider serviced this call, e.g. "EXOTEL". "STUB" until one is wired up.
    @Column(length = 30, nullable = false)
    private String provider = "STUB";

    private Instant connectedAt;

    private Instant endedAt;

    private Integer durationSeconds;
}