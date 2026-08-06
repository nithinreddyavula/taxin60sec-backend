package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.HealthCheckLead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthCheckLeadRepository extends JpaRepository<HealthCheckLead, Long> {
}