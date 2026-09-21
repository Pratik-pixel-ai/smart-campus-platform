package com.smartcampus.dto.ai;

public record AiChatResponse(
        String answer,
        boolean configured,
        String model
) {
}
