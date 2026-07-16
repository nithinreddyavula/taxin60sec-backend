package com.taxin60sec.backend.mapper;

import com.taxin60sec.backend.dto.contact.ContactRequest;
import com.taxin60sec.backend.dto.contact.ContactResponse;
import com.taxin60sec.backend.entity.Contact;
import com.taxin60sec.backend.utils.TextUtils;
import org.springframework.stereotype.Component;

@Component
public class ContactMapper {
    public Contact toEntity(ContactRequest request) {
        Contact contact = new Contact();
        contact.setName(TextUtils.normalize(request.name()));
        contact.setEmail(TextUtils.normalizeEmail(request.email()));
        contact.setMessage(TextUtils.normalize(request.message()));
        return contact;
    }

    public ContactResponse toResponse(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getName(),
                contact.getEmail(),
                contact.getMessage(),
                contact.getCreatedAt()
        );
    }
}
