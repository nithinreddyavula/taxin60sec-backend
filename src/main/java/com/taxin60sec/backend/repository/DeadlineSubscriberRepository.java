package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.DeadlineSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeadlineSubscriberRepository extends JpaRepository<DeadlineSubscriber, Long> {

    Optional<DeadlineSubscriber> findByPhoneNumber(String phoneNumber);

    List<DeadlineSubscriber> findByActiveTrue();
}