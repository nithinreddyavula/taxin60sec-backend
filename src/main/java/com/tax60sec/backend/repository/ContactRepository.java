package com.tax60sec.backend.repository;

import com.tax60sec.backend.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository
        extends JpaRepository<Contact, Long> {
}