package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import com.taxin60sec.backend.entity.enums.ServiceCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "service_offerings")
public class ServiceOffering extends BaseEntity {
    @Column(nullable = false, length = 140)
    private String name;

    @Column(nullable = false, length = 80)
    @Enumerated(EnumType.STRING)
    private ServiceCategory category = ServiceCategory.OTHER;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
