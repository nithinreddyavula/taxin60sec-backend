package com.taxin60sec.backend.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {

    private String reply;

    private boolean conversationCompleted;
}