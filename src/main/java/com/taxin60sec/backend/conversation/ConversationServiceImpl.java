package com.taxin60sec.backend.conversation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import com.taxin60sec.backend.entity.enums.ConversationState;
import com.taxin60sec.backend.entity.enums.WorkflowStage;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.workflow.WorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taxin60sec.backend.document.RequiredDocumentGeneratorService;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private final CaseRepository caseRepository;
private final ObjectMapper objectMapper;
private final WorkflowService workflowService;
private final RequiredDocumentGeneratorService requiredDocumentGeneratorService;

    public ConversationServiceImpl(
        CaseRepository caseRepository,
        ObjectMapper objectMapper,
        RequiredDocumentGeneratorService requiredDocumentGeneratorService,
        WorkflowService workflowService) {

    this.caseRepository = caseRepository;
    this.objectMapper = objectMapper;
    this.requiredDocumentGeneratorService = requiredDocumentGeneratorService;
    this.workflowService = workflowService;
}

    @Override
    public ConversationSession startConversation(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));

        String intakeQuestions = "";
        if (caseEntity.getServiceOffering() != null) {
            intakeQuestions = caseEntity.getServiceOffering().getIntakeQuestions();
        }

        List<String> questionsList = new ArrayList<>();
        if (intakeQuestions != null && !intakeQuestions.isBlank()) {
            for (String line : intakeQuestions.split("\\r?\\n")) {
                if (!line.trim().isEmpty()) {
                    questionsList.add(line.trim());
                }
            }
        }

        
        caseEntity.setIntakeAnswers("[]");
        caseEntity.setIntakeCompleted(false);
        caseEntity.setIntakeSummary(null);
        caseEntity.setConversationState(
        ConversationState.COLLECTING_INFORMATION
);
        caseRepository.save(caseEntity);


        ConversationSession session = new ConversationSession();
        session.setCaseId(caseId);
        session.setCurrentQuestionIndex(0);
        session.setQuestions(questionsList);
        session.setAnswers(new ArrayList<>());
        session.setCompleted(false);

        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public String getCurrentQuestion(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));

        String intakeQuestions = "";
        if (caseEntity.getServiceOffering() != null) {
            intakeQuestions = caseEntity.getServiceOffering().getIntakeQuestions();
        }

        List<String> questionsList = new ArrayList<>();
        if (intakeQuestions != null && !intakeQuestions.isBlank()) {
            for (String line : intakeQuestions.split("\\r?\\n")) {
                if (!line.trim().isEmpty()) {
                    questionsList.add(line.trim());
                }
            }
        }

        List<QuestionAnswer> answersList = new ArrayList<>();
        String answersJson = caseEntity.getIntakeAnswers();
        if (answersJson != null && !answersJson.isBlank()) {
            try {
                answersList = objectMapper.readValue(answersJson, new TypeReference<List<QuestionAnswer>>() {});
            } catch (Exception e) {
                // handle error or log gracefully
            }
        }

        int answeredCount = answersList.size();
        if (answeredCount < questionsList.size()) {
            return questionsList.get(answeredCount);
        }

        return null;
    }

    @Override
public ConversationSession submitAnswer(Long caseId, String answer) {

    Case caseEntity = caseRepository.findById(caseId)
            .orElseThrow(() ->
                    new RuntimeException("Case not found with ID: " + caseId));

    String intakeQuestions = "";
    if (caseEntity.getServiceOffering() != null) {
        intakeQuestions = caseEntity.getServiceOffering().getIntakeQuestions();
    }

    List<String> questionsList = new ArrayList<>();
    if (intakeQuestions != null && !intakeQuestions.isBlank()) {
        for (String line : intakeQuestions.split("\\r?\\n")) {
            if (!line.trim().isEmpty()) {
                questionsList.add(line.trim());
            }
        }
    }

    List<QuestionAnswer> answersList = new ArrayList<>();
    String answersJson = caseEntity.getIntakeAnswers();

    if (answersJson != null && !answersJson.isBlank()) {
        try {
            answersList = objectMapper.readValue(
                    answersJson,
                    new TypeReference<List<QuestionAnswer>>() {
                    });
        } catch (Exception e) {
            // handle gracefully
        }
    }

    int answeredCount = answersList.size();

    if (answeredCount >= questionsList.size()) {
        throw new RuntimeException("All questions have already been answered");
    }

    String currentQuestion = questionsList.get(answeredCount);

    answersList.add(new QuestionAnswer(currentQuestion, answer));

    try {
        String updatedJson = objectMapper.writeValueAsString(answersList);
        caseEntity.setIntakeAnswers(updatedJson);
    } catch (Exception e) {
        throw new RuntimeException(
                "Failed to serialize answers list to JSON",
                e
        );
    }

    boolean intakeCompleted = answersList.size() == questionsList.size();

    if (intakeCompleted) {

        caseEntity.setIntakeCompleted(true);

        caseEntity.setIntakeSummary(
                generateSummaryFromAnswers(answersList));

    }

    caseRepository.save(caseEntity);

    if (intakeCompleted) {

        caseEntity = workflowService.transition(
                caseId,
                WorkflowStage.DOCUMENTS_PENDING,
                "Client completed intake questionnaire",
                null
        );

        requiredDocumentGeneratorService.generateForCase(caseId);
    }

    ConversationSession session = new ConversationSession();

    session.setCaseId(caseId);
    session.setCurrentQuestionIndex(answersList.size());
    session.setQuestions(questionsList);

    List<String> answerStrings = new ArrayList<>();

    for (QuestionAnswer qa : answersList) {
        answerStrings.add(qa.getAnswer());
    }

    session.setAnswers(answerStrings);
    session.setCompleted(caseEntity.isIntakeCompleted());

    return session;
}

    @Override
    @Transactional(readOnly = true)
    public boolean isConversationCompleted(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));
        return caseEntity.isIntakeCompleted();
    }

    @Override
    @Transactional(readOnly = true)
    public String generateSummary(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));

        List<QuestionAnswer> answersList = new ArrayList<>();
        String answersJson = caseEntity.getIntakeAnswers();
        if (answersJson != null && !answersJson.isBlank()) {
            try {
                answersList = objectMapper.readValue(answersJson, new TypeReference<List<QuestionAnswer>>() {});
            } catch (Exception e) {
                // handle error or log gracefully
            }
        }

        return generateSummaryFromAnswers(answersList);
    }

    private String generateSummaryFromAnswers(List<QuestionAnswer> answersList) {
        StringBuilder sb = new StringBuilder();
        for (QuestionAnswer qa : answersList) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(qa.getQuestion()).append(" : ").append(qa.getAnswer());
        }
        return sb.toString();
    }
}
