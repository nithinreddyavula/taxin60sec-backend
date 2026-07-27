package com.taxin60sec.backend.document;

import com.taxin60sec.backend.entity.UploadedDocument;
import com.taxin60sec.backend.security.UserPrincipal;

public interface DocumentAccessService {

    void verifyAccess(UserPrincipal principal,
                      UploadedDocument document);

}