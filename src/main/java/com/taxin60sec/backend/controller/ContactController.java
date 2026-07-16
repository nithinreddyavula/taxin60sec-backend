package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.contact.ContactRequest;
import com.taxin60sec.backend.dto.contact.ContactResponse;
import com.taxin60sec.backend.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContactResponse>> create(
            @Valid @RequestBody ContactRequest request,
            HttpServletRequest httpRequest
    ) {
        ContactResponse response = contactService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Contact inquiry created", response, httpRequest.getRequestURI()));
    }

    @GetMapping
    public ApiResponse<List<ContactResponse>> findAll(HttpServletRequest request) {
        return ApiResponse.success("Contact inquiries retrieved", contactService.findAll(), request.getRequestURI());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, HttpServletRequest request) {
        contactService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Contact inquiry deleted", null, request.getRequestURI()));
    }
}
