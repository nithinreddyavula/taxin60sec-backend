package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "repatriation_records")
public class RepatriationRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @NotNull
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amountUsd;

    @NotNull
    @Column(nullable = false)
    private LocalDate transactionDate;

    @Size(max = 500)
    @Column(length = 500)
    private String purpose;

    @Column(nullable = false)
    private boolean form15caFiled = false;
}
