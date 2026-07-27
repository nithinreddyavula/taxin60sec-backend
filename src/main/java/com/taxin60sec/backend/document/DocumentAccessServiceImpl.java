package com.taxin60sec.backend.document;

import com.taxin60sec.backend.entity.Role;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.security.ForbiddenException;
import com.taxin60sec.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.taxin60sec.backend.entity.UploadedDocument;

@Service
@RequiredArgsConstructor
public class DocumentAccessServiceImpl implements DocumentAccessService {

    @Override
    public void verifyAccess(UserPrincipal principal,
                             UploadedDocument document) {

        if (principal == null) {
            throw new ForbiddenException("Access denied.");
        }

        User user = principal.getUser();

        boolean isAdmin = user.getRoles()
                .stream()
                .map(Role::getName)
                .anyMatch(role ->
                        role.equalsIgnoreCase("ROLE_ADMIN")
                                || role.equalsIgnoreCase("ADMIN"));

        if (isAdmin) {
            return;
        }

        if (document.getTaxCase()
                .getClient()
                .getId()
                .equals(user.getId())) {
            return;
        }

        throw new ForbiddenException("Access denied.");
    }
}