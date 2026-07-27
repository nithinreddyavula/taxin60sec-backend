package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.TaxHealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaxHealthCheckRepository extends JpaRepository<TaxHealthCheck, Long> {

    Optional<TaxHealthCheck> findByShareToken(String shareToken);

    boolean existsByReferralCode(String referralCode);

    long countByReferredByCode(String referredByCode);
}
