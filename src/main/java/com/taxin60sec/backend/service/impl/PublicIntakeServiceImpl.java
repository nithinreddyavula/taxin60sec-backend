package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.publicintake.PublicStartRequest;
import com.taxin60sec.backend.dto.publicintake.PublicStartResponse;
import com.taxin60sec.backend.service.PublicIntakeService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taxin60sec.backend.dto.business.CaseRequests;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.ClientProfile;
import com.taxin60sec.backend.entity.Role;
import com.taxin60sec.backend.entity.ServiceOffering;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.CasePriority;
import com.taxin60sec.backend.repository.ClientProfileRepository;
import com.taxin60sec.backend.repository.RoleRepository;
import com.taxin60sec.backend.repository.ServiceOfferingRepository;
import com.taxin60sec.backend.repository.UploadedDocumentRepository;
import com.taxin60sec.backend.repository.RequiredDocumentRepository;
import com.taxin60sec.backend.document.DocumentService;
import com.taxin60sec.backend.repository.UserRepository;
import com.taxin60sec.backend.service.BusinessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxin60sec.backend.service.NotificationService;
import com.taxin60sec.backend.repository.CaseRepository;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;

import org.springframework.http.HttpStatus;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.web.multipart.MultipartFile;

import com.taxin60sec.backend.dto.publicintake.PublicAnswerRequest;
import com.taxin60sec.backend.dto.publicintake.PublicAnswerResponse;
import com.taxin60sec.backend.dto.publicintake.NextAnswerRequest;
import com.taxin60sec.backend.dto.publicintake.ResumeIntakeResponse;
import com.taxin60sec.backend.dto.publicintake.RequiredDocumentResponse;
import com.taxin60sec.backend.document.DocumentValidationResult;
import com.taxin60sec.backend.document.DocumentUploadRequest;

import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.common.ApiErrorCode;

import com.taxin60sec.backend.entity.RequiredDocument;

@Service
@Transactional
public class PublicIntakeServiceImpl implements PublicIntakeService {

    private static final Logger log = LoggerFactory.getLogger(PublicIntakeServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    

    private final PasswordEncoder passwordEncoder;

    private final BusinessService businessService;
    private final ObjectMapper objectMapper;
    private final CaseRepository caseRepository;
    private final NotificationService notificationService;
    @Value("${app.public-url}")
private String publicUrl;

private final DocumentService documentService;

private final RequiredDocumentRepository requiredDocumentRepository;

private final UploadedDocumentRepository uploadedDocumentRepository;

    public PublicIntakeServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            ClientProfileRepository clientProfileRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            PasswordEncoder passwordEncoder,
            BusinessService businessService,
            ObjectMapper objectMapper,
            CaseRepository caseRepository,
            NotificationService notificationService,
            DocumentService documentService,
RequiredDocumentRepository requiredDocumentRepository,
UploadedDocumentRepository uploadedDocumentRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.passwordEncoder = passwordEncoder;
        this.businessService = businessService;
        this.objectMapper = objectMapper;
        this.caseRepository=caseRepository;
        this.notificationService=notificationService;
        this.documentService = documentService;
this.requiredDocumentRepository = requiredDocumentRepository;
this.uploadedDocumentRepository = uploadedDocumentRepository;
    }
    @Override
public PublicStartResponse start(PublicStartRequest request) {

    User client = findOrCreateClient(request);

    ServiceOffering service =
        serviceOfferingRepository
                .findById(request.serviceOfferingId())
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                ApiErrorCode.NOT_FOUND,
                                "Service not found"
                        )
                );

   Case taxCase = businessService.createCaseEntity(

        new CaseRequests.Create(

                service.getDisplayName() + " Intake",
                null,
                service.getId(),
                CasePriority.NORMAL,
                null,
                null

        ),

        client

);

taxCase.setPublicAccessToken(UUID.randomUUID().toString());

taxCase.setPublicAccessExpiry(
        LocalDateTime.now().plusDays(30)
);

caseRepository.save(taxCase);
String resumeUrl =
        publicUrl +
        "/intake/resume/" +
        taxCase.getPublicAccessToken();

/*notificationService.sendResumeEmail(

        client.getEmail(),

        client.getFullName(),

        resumeUrl

);

notificationService.sendResumeWhatsApp(

        client.getPhoneNumber(),

        client.getFullName(),

        resumeUrl

);*/

    List<String> questions = List.of();

    if (service.getIntakeQuestions() != null &&
            !service.getIntakeQuestions().isBlank()) {

        questions = Arrays.stream(
                        service.getIntakeQuestions().split("\\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    return new PublicStartResponse(

            taxCase.getId(),
            taxCase.getPublicAccessToken(),
            client.getFullName(),
            service.getDisplayName(),
            questions,
            new HashMap<>()

    );
}
private User findOrCreateClient(PublicStartRequest request) {

    Optional<User> existing =
            userRepository.findByPhoneNumber(request.phoneNumber());

    if (existing.isPresent()) {

        User user = existing.get();

        user.setFullName(request.fullName());
        user.setEmail(request.email());

        return userRepository.save(user);
    }

    return createClient(request);
}
private User createClient(PublicStartRequest request) {

    Role clientRole = roleRepository
            .findByName("ROLE_CLIENT")
            .orElseThrow(() ->
                    new ApiException( HttpStatus.NOT_FOUND,
        ApiErrorCode.NOT_FOUND,
        "ROLE_CLIENT not found"));

    Optional<User> existingEmail =
        userRepository.findByEmail(request.email());

if (existingEmail.isPresent()) {
    throw new ApiException(
            HttpStatus.BAD_REQUEST,
            ApiErrorCode.BAD_REQUEST,
            "Email already belongs to another account."
    );
}

    User user = new User();

    user.setFullName(request.fullName());
    user.setEmail(request.email());
    user.setPhoneNumber(request.phoneNumber());

    user.setPasswordHash(
            passwordEncoder.encode(UUID.randomUUID().toString())
    );

    user.setActive(true);
    user.getRoles().add(clientRole);

    user = userRepository.save(user);

    ClientProfile profile = new ClientProfile();
    profile.setUser(user);

    clientProfileRepository.save(profile);

    return user;

}
private Map<String,String> readAnswers(Case taxCase){

    try{

        if(taxCase.getIntakeAnswers()==null
                || taxCase.getIntakeAnswers().isBlank()){

            return new LinkedHashMap<>();

        }

        return objectMapper.readValue(

                taxCase.getIntakeAnswers(),

                new TypeReference<
                        LinkedHashMap<String,String>>() {}

        );

    }
    catch(Exception ex){

        throw new RuntimeException(ex);

    }

}
private void writeAnswers(

        Case taxCase,

        Map<String,String> answers

){

    try{

        taxCase.setIntakeAnswers(

                objectMapper.writeValueAsString(
                        answers
                )

        );

    }
    catch(Exception ex){

        throw new RuntimeException(ex);

    }

}
@Override
public PublicAnswerResponse saveAnswer(
        Long caseId,
        PublicAnswerRequest request
) {

    Case taxCase = caseRepository.findById(caseId)
            .orElseThrow(() ->
                    new ApiException(
                            HttpStatus.NOT_FOUND,
                            ApiErrorCode.NOT_FOUND,
                            "Case not found"
                    ));

    Map<String, String> answers = readAnswers(taxCase);

    answers.put(
            request.question(),
            request.answer()
    );

    writeAnswers(
            taxCase,
            answers
    );

    ServiceOffering service =
            taxCase.getServiceOffering();

    List<String> questions = List.of();

    if (service.getIntakeQuestions() != null &&
            !service.getIntakeQuestions().isBlank()) {

        questions = Arrays.stream(
                        service.getIntakeQuestions().split("\\n"))
                .map(String::trim)
                .filter(q -> !q.isBlank())
                .toList();
    }

    String nextQuestion = null;

    for (String q : questions) {

        if (!answers.containsKey(q)) {

            nextQuestion = q;
            break;

        }

    }

    boolean completed = nextQuestion == null;

    taxCase.setIntakeCompleted(completed);

    caseRepository.save(taxCase);

    return new PublicAnswerResponse(

            taxCase.getId(),

            completed,

            answers.size(),

            questions.size(),

            nextQuestion,

            answers

    );

}
@Override
public PublicAnswerResponse next(

        Long caseId,

        NextAnswerRequest request

) {

    Case taxCase = caseRepository.findById(caseId)
            .orElseThrow(() ->
                    new ApiException(
                            HttpStatus.NOT_FOUND,
                            ApiErrorCode.NOT_FOUND,
                            "Case not found"
                    ));

    ServiceOffering service =
            taxCase.getServiceOffering();

    List<String> questions = Arrays.stream(
                    service.getIntakeQuestions().split("\\n"))
            .map(String::trim)
            .filter(q -> !q.isBlank())
            .toList();

    Map<String,String> answers =
            readAnswers(taxCase);

    String currentQuestion = null;

    for(String q : questions){

        if(!answers.containsKey(q)){

            currentQuestion = q;
            break;

        }

    }

    if(currentQuestion == null){

        return new PublicAnswerResponse(

                taxCase.getId(),

                true,

                answers.size(),

                questions.size(),

                null,

                answers

        );

    }

    answers.put(

            currentQuestion,

            request.answer()

    );

    writeAnswers(

            taxCase,

            answers

    );

    String nextQuestion = null;

    for(String q : questions){

        if(!answers.containsKey(q)){

            nextQuestion = q;
            break;

        }

    }

    boolean completed =
            nextQuestion == null;

    taxCase.setIntakeCompleted(completed);

    caseRepository.save(taxCase);

    return new PublicAnswerResponse(

            taxCase.getId(),

            completed,

            answers.size(),

            questions.size(),

            nextQuestion,

            answers

    );

}
@Override
public ResumeIntakeResponse resume(String token) {

    Case taxCase = caseRepository
            .findByPublicAccessToken(token)
            .orElseThrow(() ->
                    new ApiException(
                            HttpStatus.NOT_FOUND,
                            ApiErrorCode.NOT_FOUND,
                            "Resume link not found"
                    ));

    if (taxCase.getPublicAccessExpiry() != null &&
            taxCase.getPublicAccessExpiry().isBefore(LocalDateTime.now())) {

        throw new ApiException(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.BAD_REQUEST,
                "Resume link expired"
        );
    }

    Map<String,String> answers =
            readAnswers(taxCase);

    List<String> questions = Arrays.stream(
                    taxCase.getServiceOffering()
                            .getIntakeQuestions()
                            .split("\\n"))
            .map(String::trim)
            .filter(q -> !q.isBlank())
            .toList();

    String currentQuestion = null;

    for(String q : questions){

        if(!answers.containsKey(q)){

            currentQuestion = q;
            break;

        }

    }

    return new ResumeIntakeResponse(

            taxCase.getId(),

            taxCase.getClient().getFullName(),

            taxCase.getServiceOffering().getDisplayName(),

            taxCase.isIntakeCompleted(),

            answers.size(),

            questions.size(),

            currentQuestion,

            answers

    );

}
@Override
public List<RequiredDocumentResponse> getRequiredDocuments(Long caseId) {

    Case taxCase = caseRepository.findById(caseId)
            .orElseThrow(() -> new EntityNotFoundException("Case not found"));

    List<RequiredDocument> docs =
            requiredDocumentRepository
                    .findByTaxCaseIdAndDeletedFalseOrderByDisplayOrderAsc(caseId);

    if (docs.isEmpty() && taxCase.getServiceOffering() != null) {

        docs = requiredDocumentRepository
                .findByServiceOfferingIdAndDeletedFalseOrderByDisplayOrderAsc(
                        taxCase.getServiceOffering().getId()
                );
    }

    if (docs.isEmpty() && taxCase.getServiceOffering() != null) {

        docs = createDefaultRequiredDocuments(taxCase);
    }

    return docs.stream()
            .map(doc -> {

                boolean uploaded =
                        uploadedDocumentRepository
                                .existsByTaxCaseIdAndRequiredDocumentIdAndDeletedFalse(
                                        caseId,
                                        doc.getId()
                                );

                return new RequiredDocumentResponse(
                        doc.getId(),
                        doc.getName(),
                        doc.isMandatory(),
                        uploaded
                );
            })
            .toList();
}

/**
 * Every service is expected to have its own required-document checklist configured via
 * the catalog admin API, but there is currently no UI for that. As a fallback, this
 * generates a sensible default checklist the first time a case needs one, scoped to
 * this case, and persists it so future loads (and uploads) work normally. Every
 * generated document is optional (mandatory=false) so a client can always submit
 * without being blocked on document uploads.
 */
private List<RequiredDocument> createDefaultRequiredDocuments(Case taxCase) {

    List<String[]> defaults = defaultChecklistFor(taxCase.getServiceOffering().getCategory());

    List<RequiredDocument> created = new java.util.ArrayList<>();

    int order = 0;

    for (String[] entry : defaults) {

        RequiredDocument d = new RequiredDocument();
        d.setName(entry[0]);
        d.setDocumentType(entry[1]);
        d.setDescription(entry[2]);
        d.setMandatory(false);
        d.setAcceptedFileTypes("application/pdf,image/jpeg,image/png");
        d.setMaximumFileSize(10_485_760L);
        d.setDisplayOrder(order++);
        d.setTaxCase(taxCase);

        created.add(requiredDocumentRepository.save(d));
    }

    return created;
}

private List<String[]> defaultChecklistFor(com.taxin60sec.backend.entity.enums.ServiceCategory category) {

    return switch (category) {

        case GST -> List.of(
                new String[]{"GST Registration Certificate", "GST_CERTIFICATE", "Existing GST registration certificate, if already registered."},
                new String[]{"PAN Card", "PAN_CARD", "PAN card of the business or proprietor."},
                new String[]{"Bank Statement (last 3 months)", "BANK_STATEMENT", "Recent bank statement for the business account."},
                new String[]{"Sales / Purchase Invoices", "INVOICES", "Invoices for the period being filed."}
        );

        case INCOME_TAX -> List.of(
                new String[]{"PAN Card", "PAN_CARD", "PAN card of the taxpayer."},
                new String[]{"Form 16 / Salary Slips", "FORM_16", "Form 16 from employer, or recent salary slips."},
                new String[]{"Bank Statement", "BANK_STATEMENT", "Bank statement for the assessment year."},
                new String[]{"Investment Proofs", "INVESTMENT_PROOF", "Proofs for 80C and other applicable deductions."}
        );

        case STARTUP -> List.of(
                new String[]{"Founders' PAN & Aadhaar", "IDENTITY_PROOF", "Identity proof for all founders/directors."},
                new String[]{"Registered Office Proof", "ADDRESS_PROOF", "Utility bill or rent agreement for the registered address."},
                new String[]{"Digital Signature Certificate", "DSC", "If already obtained; otherwise can be arranged separately."}
        );

        case NRI -> List.of(
                new String[]{"Passport / OCI Card", "IDENTITY_PROOF", "Copy of passport showing residency status, or OCI card."},
                new String[]{"PAN Card", "PAN_CARD", "Indian PAN card, mandatory for any Indian tax filing."},
                new String[]{"NRE / NRO Bank Statements", "BANK_STATEMENT", "Statements for the financial year for all NRE/NRO accounts."},
                new String[]{"Foreign Income / TRC", "FOREIGN_INCOME_PROOF", "Tax Residency Certificate and proof of foreign income, for DTAA benefit claims."},
                new String[]{"Property Documents", "PROPERTY_DOCS", "If applicable - sale deed, purchase deed, or rental agreement for Indian property."}
        );

        case ADVISORY -> List.of(
                new String[]{"Latest Financial Statements", "FINANCIAL_STATEMENTS", "Most recent P&L and balance sheet."},
                new String[]{"Bank Statements (last 6 months)", "BANK_STATEMENT", "Recent bank statements for cash flow review."}
        );

        case COMPLIANCE -> List.of(
                new String[]{"Previous Audit Report", "AUDIT_REPORT", "If available, from the prior financial year."},
                new String[]{"Financial Statements", "FINANCIAL_STATEMENTS", "Current year financial statements."},
                new String[]{"Statutory Registers", "STATUTORY_REGISTERS", "Company statutory registers, if applicable."}
        );

        case ACCOUNTING -> List.of(
                new String[]{"Existing Books of Accounts", "BOOKS_OF_ACCOUNTS", "Current bookkeeping records or export."},
                new String[]{"Bank Statements", "BANK_STATEMENT", "Recent bank statements for reconciliation."},
                new String[]{"Sales / Purchase Invoices", "INVOICES", "Invoices for automation setup."}
        );

        default -> List.of(
                new String[]{"Identity Proof", "IDENTITY_PROOF", "Any government-issued identity proof."},
                new String[]{"Supporting Documents", "SUPPORTING_DOCS", "Any other documents relevant to your request."}
        );
    };
}
@Override
public void uploadDocument(

        Long caseId,

        Long requiredDocumentId,

        MultipartFile file

) {

    DocumentUploadRequest request = new DocumentUploadRequest();

    request.setCaseId(caseId);

    request.setRequiredDocumentId(requiredDocumentId);

    request.setFile(file);

    documentService.upload(request);

}
@Override
public DocumentValidationResult validateDocuments(Long caseId) {

    return documentService.validate(caseId);

}
@Override
@Transactional
public void submitCase(Long caseId) {

    Case taxCase = caseRepository.findById(caseId)

            .orElseThrow(() ->

                    new EntityNotFoundException("Case not found"));

    DocumentValidationResult result =
            documentService.validate(caseId);

    if (!result.isValid()) {

        throw new IllegalStateException(result.getMessage());

    }

    taxCase.setIntakeCompleted(true);

    caseRepository.save(taxCase);

    notifySubmission(taxCase);

}

private void notifySubmission(Case taxCase) {

    String caseNumber = taxCase.getCaseNumber();
    String serviceName = taxCase.getServiceOffering() != null ? taxCase.getServiceOffering().getDisplayName() : "N/A";
    var client = taxCase.getClient();
    String clientName = client != null ? client.getFullName() : "N/A";
    String clientEmail = client != null ? client.getEmail() : null;
    String clientPhone = client != null ? client.getPhoneNumber() : null;

    if (clientEmail != null && !clientEmail.isBlank()) {
        try {
            notificationService.sendSubmissionConfirmationEmail(clientEmail, clientName, caseNumber, serviceName);
        } catch (Exception ex) {
            log.warn("Failed to send submission confirmation email for case {}", caseNumber, ex);
        }
    }

    if (clientPhone != null && !clientPhone.isBlank()) {
        try {
            notificationService.sendSubmissionConfirmationWhatsApp(clientPhone, clientName, caseNumber);
        } catch (Exception ex) {
            log.warn("Failed to send submission confirmation WhatsApp message for case {}", caseNumber, ex);
        }
    }

    try {
        notificationService.sendAdminNewSubmissionEmail(clientName, clientEmail, clientPhone, serviceName, caseNumber);
    } catch (Exception ex) {
        log.warn("Failed to send admin submission alert email for case {}", caseNumber, ex);
    }

    try {
        notificationService.sendAdminNewSubmissionWhatsApp(clientName, serviceName, caseNumber);
    } catch (Exception ex) {
        log.warn("Failed to send admin submission alert WhatsApp message for case {}", caseNumber, ex);
    }

}
}