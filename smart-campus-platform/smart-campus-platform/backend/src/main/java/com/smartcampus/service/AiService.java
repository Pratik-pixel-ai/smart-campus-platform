package com.smartcampus.service;

import com.smartcampus.dto.ai.AiChatRequest;
import com.smartcampus.dto.ai.AiChatResponse;
import com.smartcampus.dto.ai.AiStatusResponse;

/**
 * Campus assistant. An interface so the provider can be swapped without changing
 * the controller; GroqAiService is the only implementation today.
 *
 * The assistant is read-only by design: the backend decides what campus data goes
 * into the prompt, and the model can only answer with text.
 */
public interface AiService {

    AiChatResponse ask(AiChatRequest request);

    AiStatusResponse status();
}
