package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "client_profiles")
public class ClientProfile extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 180)
    @Column(length = 180)
    private String businessName;

    @Size(max = 40)
    @Column(length = 40)
    private String panNumber;

    @Size(max = 40)
    @Column(length = 40)
    private String gstin;

    @Size(max = 1000)
    @Column(length = 1000)
    private String address;
}
