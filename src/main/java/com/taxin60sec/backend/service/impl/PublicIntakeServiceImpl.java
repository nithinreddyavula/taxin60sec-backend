package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.publicintake.PublicStartRequest;
import com.taxin60sec.backend.dto.publicintake.PublicStartResponse;
import com.taxin60sec.backend.service.PublicIntakeService;
import org.springframework.stereotype.Service;
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

    System.out.println("STEP 1");

    Case taxCase = caseRepository.findById(caseId)
            .orElseThrow(() -> new EntityNotFoundException("Case not found"));

    System.out.println("STEP 2");

    List<RequiredDocument> docs =
            requiredDocumentRepository
                    .findByTaxCaseIdAndDeletedFalseOrderByDisplayOrderAsc(caseId);

    System.out.println("STEP 3 docs=" + docs.size());

    if (docs.isEmpty() && taxCase.getServiceOffering() != null) {

        System.out.println("STEP 4");

        docs = requiredDocumentRepository
                .findByServiceOfferingIdAndDeletedFalseOrderByDisplayOrderAsc(
                        taxCase.getServiceOffering().getId()
                );

        System.out.println("STEP 5 docs=" + docs.size());
    }

    System.out.println("STEP 6");

    return docs.stream()
            .map(doc -> {

                System.out.println("Checking upload for doc " + doc.getId());

                boolean uploaded =
                        uploadedDocumentRepository
                                .existsByTaxCaseIdAndRequiredDocumentIdAndDeletedFalse(
                                        caseId,
                                        doc.getId()
                                );

                System.out.println("Uploaded = " + uploaded);

                return new RequiredDocumentResponse(
                        doc.getId(),
                        doc.getName(),
                        doc.isMandatory(),
                        uploaded
                );
            })
            .toList();
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

}
}