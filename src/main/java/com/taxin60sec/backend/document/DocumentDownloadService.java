package com.taxin60sec.backend.document;
import com.taxin60sec.backend.security.UserPrincipal;
import org.springframework.core.io.Resource;

public interface DocumentDownloadService {

    Resource download(Long uploadedDocumentId,
                      UserPrincipal principal);

}