package com.smartcampus.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
        @NotBlank(message = "Ask a question to continue")
        @Size(max = 500, message = "Question can be at most 500 characters")
        String question
) {
}
