package com.tax60sec.backend.controller;

import com.tax60sec.backend.entity.Contact;
import com.tax60sec.backend.repository.ContactRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {

    private final ContactRepository repository;

    public ContactController(ContactRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Contact saveContact(@RequestBody Contact contact) {

        Contact saved = repository.save(contact);

        System.out.println("SAVED ID = " + saved.getId());

        return saved;
    }

    @GetMapping
    public List<Contact> getContacts() {

        List<Contact> contacts = repository.findAll();

        System.out.println("CONTACT COUNT = " + contacts.size());

        return contacts;
    }

    @DeleteMapping("/{id}")
    public void deleteContact(@PathVariable Long id) {
        repository.deleteById(id);
    }
}