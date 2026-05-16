package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.entity.Contact;
import com.taxin60sec.backend.repository.ContactRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactRepository contactRepository;

    public ContactController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @PostMapping
    public Contact saveContact(@RequestBody Contact contact) {
        return contactRepository.save(contact);
    }
    @GetMapping
public List<Contact> getAllContacts() {
    return contactRepository.findAll();
}
@DeleteMapping("/{id}")
public void deleteContact(@PathVariable Long id) {
    contactRepository.deleteById(id);
}
}