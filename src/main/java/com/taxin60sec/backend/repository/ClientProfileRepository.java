package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientProfileRepository extends JpaRepository<ClientProfile, Long> {
}
