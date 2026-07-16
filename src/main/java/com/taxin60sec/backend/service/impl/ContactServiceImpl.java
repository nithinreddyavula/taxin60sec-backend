package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.contact.ContactRequest;
import com.taxin60sec.backend.dto.contact.ContactResponse;
import com.taxin60sec.backend.exception.ResourceNotFoundException;
import com.taxin60sec.backend.mapper.ContactMapper;
import com.taxin60sec.backend.repository.ContactRepository;
import com.taxin60sec.backend.service.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ContactServiceImpl implements ContactService {
    private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;

    public ContactServiceImpl(ContactRepository contactRepository, ContactMapper contactMapper) {
        this.contactRepository = contactRepository;
        this.contactMapper = contactMapper;
    }

    @Override
    @Transactional
    public ContactResponse create(ContactRequest request) {
        ContactResponse response = contactMapper.toResponse(contactRepository.save(contactMapper.toEntity(request)));
        log.info("Created contact inquiry id={}", response.id());
        return response;
    }

    @Override
    public List<ContactResponse> findAll() {
        return contactRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(contactMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!contactRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contact inquiry not found");
        }
        contactRepository.deleteById(id);
        log.info("Deleted contact inquiry id={}", id);
    }
}
