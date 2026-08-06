package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.ClientCaseRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientCaseRatingRepository extends JpaRepository<ClientCaseRating, Long> {
    Optional<ClientCaseRating> findByCaseRef_Id(Long caseId);
}