package com.taxin60sec.backend.service;

import com.taxin60sec.backend.dto.contact.ContactRequest;
import com.taxin60sec.backend.dto.contact.ContactResponse;

import java.util.List;

public interface ContactService {
    ContactResponse create(ContactRequest request);

    List<ContactResponse> findAll();

    void deleteById(Long id);
}
