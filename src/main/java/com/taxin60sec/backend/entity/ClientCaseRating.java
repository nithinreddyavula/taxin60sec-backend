package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "client_case_ratings", uniqueConstraints = @UniqueConstraint(columnNames = "case_id"))
public class ClientCaseRating extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false, unique = true)
    private Case caseRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer score;

    @Size(max = 2000)
    @Column(length = 2000)
    private String feedback;
}