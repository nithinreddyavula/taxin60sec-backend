package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import com.taxin60sec.backend.entity.enums.BackgroundCheckStatus;
import com.taxin60sec.backend.entity.enums.CAAvailability;
import com.taxin60sec.backend.entity.enums.CATier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "ca_profiles")
public class CAProfile extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 80)
    @Column(length = 80)
    private String membershipNumber;

    @Size(max = 10)
    @Column(length = 10)
    private String panNumber;

    @Size(max = 180)
    @Column(length = 180)
    private String firmName;

    @Size(max = 1000)
    @Column(length = 1000)
    private String specialization;

    @Column(nullable = false)
    private boolean verified = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CATier tier;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private BackgroundCheckStatus backgroundCheckStatus = BackgroundCheckStatus.PENDING;

    // Storage key/version pairs from StorageService.upload() - null until the CA uploads each document.
    @Column(length = 300)
    private String practiceCertificateKey;

    @Column(length = 60)
    private String practiceCertificateVersion;

    @Column(length = 300)
    private String panDocumentKey;

    @Column(length = 60)
    private String panDocumentVersion;

    // Digital acceptance of the partner agreement (anti-poaching clause, commission terms, ToS) at onboarding.
    private java.time.Instant agreementAcceptedAt;

    @Column(length = 40)
    private String agreementVersion;

    // Self-reported capacity - advisory only, see CAAvailability. Defaults to AVAILABLE so
    // newly verified CAs show up as assignable without an extra required step.
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private CAAvailability availability = CAAvailability.AVAILABLE;

    // ---------- payout destination (item #5's real money-movement leg) ----------
    // Where a CA's released earnings actually get sent. Nullable throughout: a CA can be
    // fully verified and working cases before setting this up, and PayoutProvider simply
    // fails a payout attempt with a clear reason if it's missing when a release is due -
    // it never blocks the escrow release bookkeeping itself (see EscrowService).

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private com.taxin60sec.backend.entity.enums.PayoutMethod payoutMethod;

    @Size(max = 120)
    @Column(length = 120)
    private String payoutAccountHolderName;

    @Size(max = 34)
    @Column(length = 34)
    private String payoutBankAccountNumber;

    @Size(max = 11)
    @Column(length = 11)
    private String payoutBankIfsc;

    @Size(max = 100)
    @Column(length = 100)
    private String payoutUpiId;

    // RazorpayX's own IDs for this CA's contact + fund account, once created - cached so
    // repeat payouts don't recreate them on RazorpayX's side every time.
    @Column(length = 60)
    private String payoutProviderContactId;

    @Column(length = 60)
    private String payoutProviderFundAccountId;

    public boolean payoutDestinationConfigured() {
        boolean bank = payoutBankAccountNumber != null && !payoutBankAccountNumber.isBlank()
                && payoutBankIfsc != null && !payoutBankIfsc.isBlank();
        boolean upi = payoutUpiId != null && !payoutUpiId.isBlank();
        return payoutMethod != null && (bank || upi);
    }

    public boolean documentsComplete() {
        return practiceCertificateKey != null && panDocumentKey != null;
    }
}