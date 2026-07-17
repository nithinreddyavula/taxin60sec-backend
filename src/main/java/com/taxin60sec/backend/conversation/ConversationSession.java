package com.taxin60sec.backend.conversation;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ConversationSession {

    private Long caseId;

    private int currentQuestionIndex;

    private List<String> questions = new ArrayList<>();

    private List<String> answers = new ArrayList<>();

    private boolean completed;
}