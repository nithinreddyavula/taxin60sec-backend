package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.dto.contact.ContactRequest;
import com.taxin60sec.backend.dto.contact.ContactResponse;
import com.taxin60sec.backend.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
public class LegacyContactController {
    private final ContactService contactService;

    public LegacyContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse saveContact(@Valid @RequestBody ContactRequest request) {
        return contactService.create(request);
    }

    @GetMapping
    public List<ContactResponse> getAllContacts() {
        return contactService.findAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContact(@PathVariable Long id) {
        contactService.deleteById(id);
    }
}
