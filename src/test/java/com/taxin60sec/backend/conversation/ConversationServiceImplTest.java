package com.taxin60sec.backend.conversation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.ServiceOffering;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import com.taxin60sec.backend.entity.enums.ConversationState;
import com.taxin60sec.backend.entity.enums.WorkflowStage;
import com.taxin60sec.backend.repository.CaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.taxin60sec.backend.document.RequiredDocumentGeneratorService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    private CaseRepository caseRepository;
    private ObjectMapper objectMapper;
    @Mock
private RequiredDocumentGeneratorService requiredDocumentGeneratorService;
    private ConversationServiceImpl conversationService;

    @BeforeEach
    void setUp() {
        caseRepository = mock(CaseRepository.class);
        objectMapper = new ObjectMapper();
        conversationService = new ConversationServiceImpl(
        caseRepository,
        objectMapper,
        requiredDocumentGeneratorService
);
    }

    @Test
    void startConversation_Succeeds() {
        Long caseId = 1L;
        Case taxCase = new Case();
        taxCase.setId(caseId);

        ServiceOffering offering = new ServiceOffering();
        offering.setIntakeQuestions("What is your age?\nWhat is your income?\r\nWhere do you reside?");
        taxCase.setServiceOffering(offering);

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(taxCase));

        ConversationSession session = conversationService.startConversation(caseId);

        assertNotNull(session);
        assertEquals(caseId, session.getCaseId());
        assertEquals(0, session.getCurrentQuestionIndex());
        assertEquals(3, session.getQuestions().size());
        assertEquals("What is your age?", session.getQuestions().get(0));
        assertEquals("What is your income?", session.getQuestions().get(1));
        assertEquals("Where do you reside?", session.getQuestions().get(2));
        assertTrue(session.getAnswers().isEmpty());
        assertFalse(session.isCompleted());

        assertEquals(ConversationState.COLLECTING_INFORMATION, taxCase.getConversationState());
        assertEquals("[]", taxCase.getIntakeAnswers());
        assertFalse(taxCase.isIntakeCompleted());
        assertNull(taxCase.getIntakeSummary());

        verify(caseRepository).save(taxCase);
    }

    @Test
    void getCurrentQuestion_ReturnsCorrectQuestions() {
        Long caseId = 1L;
        Case taxCase = new Case();
        taxCase.setId(caseId);

        ServiceOffering offering = new ServiceOffering();
        offering.setIntakeQuestions("Question 1\nQuestion 2");
        taxCase.setServiceOffering(offering);

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(taxCase));

        // Case 1: No answers submitted yet (intakeAnswers is empty or null)
        taxCase.setIntakeAnswers(null);
        String currentQuestion = conversationService.getCurrentQuestion(caseId);
        assertEquals("Question 1", currentQuestion);

        // Case 2: One answer submitted
        taxCase.setIntakeAnswers("[{\"question\":\"Question 1\",\"answer\":\"Answer 1\"}]");
        currentQuestion = conversationService.getCurrentQuestion(caseId);
        assertEquals("Question 2", currentQuestion);

        // Case 3: Both answers submitted
        taxCase.setIntakeAnswers("[{\"question\":\"Question 1\",\"answer\":\"Answer 1\"},{\"question\":\"Question 2\",\"answer\":\"Answer 2\"}]");
        currentQuestion = conversationService.getCurrentQuestion(caseId);
        assertNull(currentQuestion);
    }

    @Test
    void submitAnswer_SucceedsAndTriggersCompletion() {
        Long caseId = 1L;
        Case taxCase = new Case();
        taxCase.setId(caseId);
        taxCase.setStatus(CaseStatus.DRAFT);
        taxCase.setWorkflowStage(WorkflowStage.CREATED);

        ServiceOffering offering = new ServiceOffering();
        offering.setIntakeQuestions("Question 1\nQuestion 2");
        taxCase.setServiceOffering(offering);

        // Initialize with first question answered
        taxCase.setIntakeAnswers("[{\"question\":\"Question 1\",\"answer\":\"Answer 1\"}]");

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(taxCase));

        // Submit answer to the second (last) question
        ConversationSession session = conversationService.submitAnswer(caseId, "Answer 2");

        assertNotNull(session);
        assertEquals(caseId, session.getCaseId());
        assertEquals(2, session.getCurrentQuestionIndex());
        assertEquals(2, session.getAnswers().size());
        assertEquals("Answer 1", session.getAnswers().get(0));
        assertEquals("Answer 2", session.getAnswers().get(1));
        assertTrue(session.isCompleted());

        // Verify status & workflow updates
        assertEquals(ConversationState.COLLECTING_DOCUMENTS, taxCase.getConversationState());
        assertTrue(taxCase.isIntakeCompleted());
        assertEquals(WorkflowStage.DOCUMENTS_PENDING, taxCase.getWorkflowStage());
        assertEquals(CaseStatus.DOCUMENT_COLLECTION, taxCase.getStatus());
        assertEquals("Question 1 : Answer 1\nQuestion 2 : Answer 2", taxCase.getIntakeSummary());

        verify(caseRepository).save(taxCase);
    }

    @Test
    void submitAnswer_ThrowsExceptionWhenAllQuestionsAlreadyAnswered() {
        Long caseId = 1L;
        Case taxCase = new Case();
        taxCase.setId(caseId);

        ServiceOffering offering = new ServiceOffering();
        offering.setIntakeQuestions("Question 1");
        taxCase.setServiceOffering(offering);
        taxCase.setIntakeAnswers("[{\"question\":\"Question 1\",\"answer\":\"Answer 1\"}]");

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(taxCase));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            conversationService.submitAnswer(caseId, "Answer 2");
        });

        assertEquals("All questions have already been answered", exception.getMessage());
        verify(caseRepository, never()).save(any(Case.class));
    }

    @Test
    void isConversationCompleted_ReturnsCorrectValue() {
        Long caseId = 1L;
        Case taxCase = new Case();
        taxCase.setId(caseId);

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(taxCase));

        taxCase.setIntakeCompleted(false);
        assertFalse(conversationService.isConversationCompleted(caseId));

        taxCase.setIntakeCompleted(true);
        assertTrue(conversationService.isConversationCompleted(caseId));
    }

    @Test
    void generateSummary_ReturnsCorrectFormattedSummary() {
        Long caseId = 1L;
        Case taxCase = new Case();
        taxCase.setId(caseId);
        taxCase.setIntakeAnswers("[{\"question\":\"Q1\",\"answer\":\"A1\"},{\"question\":\"Q2\",\"answer\":\"A2\"}]");

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(taxCase));

        String summary = conversationService.generateSummary(caseId);
        assertEquals("Q1 : A1\nQ2 : A2", summary);
    }
}
