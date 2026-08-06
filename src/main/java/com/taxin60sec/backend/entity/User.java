package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User extends BaseEntity {
    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 180)
    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Size(max = 30)
    @Column(length = 30)
    private String phoneNumber;

    @Size(max = 120)
@Column(length = 120)
private String passwordHash;

    @Column(nullable = false)
    private boolean active = true;

    @Column(unique = true, length = 10)
    private String referralCode;

    @Column(length = 10)
    private String referredByCode;

    @Column(nullable = false, columnDefinition = "integer default 0")
private int referralCredits = 0;

    /**
     * Admin/CA-configurable repatriation tracking limit in USD for this financial year.
     * Actual applicable limits vary by transaction type (NRO balance, property sale, etc.)
     * and must be confirmed by a CA - this is a tracking aid, not a legal determination.
     */
    @Column(nullable = true)
    private java.math.BigDecimal repatriationLimitUsd;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ClientProfile clientProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private CAProfile caProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private AdminProfile adminProfile;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private Set<Case> clientCases = new HashSet<>();

    @OneToMany(mappedBy = "assignedCa", fetch = FetchType.LAZY)
    private Set<Case> assignedCases = new HashSet<>();
}