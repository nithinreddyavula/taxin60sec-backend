package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.RefreshToken;
import com.taxin60sec.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByUser(User user);
}
