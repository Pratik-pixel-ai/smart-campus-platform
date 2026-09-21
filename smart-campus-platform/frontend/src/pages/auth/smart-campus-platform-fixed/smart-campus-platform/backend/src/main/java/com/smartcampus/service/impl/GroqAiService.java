package com.smartcampus.service.impl;

import com.smartcampus.dto.ai.AiChatRequest;
import com.smartcampus.dto.ai.AiChatResponse;
import com.smartcampus.dto.ai.AiStatusResponse;
import com.smartcampus.service.AiService;
import com.smartcampus.service.CampusContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Calls the Groq chat completions API from Java. The key lives only in the backend
 * environment, so it is never shipped to the browser, and the browser can never call
 * the provider directly.
 *
 * If GROQ_API_KEY is not set the application still starts and every question gets a
 * clear "not configured" answer instead of an error.
 */
@Slf4j
@Service
public class GroqAiService implements AiService {

    private static final String SYSTEM_PROMPT = """
            You are the Smart Campus assistant for a college platform.
            Answer only from the campus data provided in the context block below.
            If the answer is not in the context, say you do not have that information.
            Never invent attendance figures, marks, deadlines or timetable entries.
            Keep answers short, factual and friendly. Use plain sentences, not markdown tables.
            """;

    private final RestTemplate restTemplate;
    private final CampusContextService campusContextService;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public GroqAiService(RestTemplate groqRestTemplate,
                         CampusContextService campusContextService,
                         @Value("${groq.api-key:}") String apiKey,
                         @Value("${groq.base-url}") String baseUrl,
                         @Value("${groq.model}") String model) {
        this.restTemplate = groqRestTemplate;
        this.campusContextService = campusContextService;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    @Override
    public AiChatResponse ask(AiChatRequest request) {
        if (!isConfigured()) {
            return new AiChatResponse(
                    "AI assistant is not configured. Please add GROQ_API_KEY.", false, model);
        }

        // Step 1-3: the backend authenticates the caller and assembles their context.
        String context = campusContextService.buildContext();

        String userMessage = """
                CAMPUS DATA
                -----------
                %s
                -----------
                QUESTION: %s
                """.formatted(context, request.question());

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.2,
                "max_tokens", 500,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userMessage))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            // Step 4: send context + question to Groq. Step 5: return the text answer.
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate
                    .postForObject(baseUrl, new HttpEntity<>(body, headers), Map.class);
            return new AiChatResponse(extractAnswer(response), true, model);
        } catch (RestClientException ex) {
            log.error("Groq request failed", ex);
            return new AiChatResponse(
                    "The assistant could not be reached right now. Please try again in a moment.", true, model);
        }
    }

    @Override
    public AiStatusResponse status() {
        return isConfigured()
                ? new AiStatusResponse(true, model, "Assistant is ready")
                : new AiStatusResponse(false, model, "AI assistant is not configured. Please add GROQ_API_KEY.");
    }

    private boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** Pulls choices[0].message.content out of the OpenAI-compatible response. */
    @SuppressWarnings("unchecked")
    private String extractAnswer(Map<String, Object> response) {
        if (response == null) {
            return "The assistant returned an empty response.";
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            return "The assistant returned an empty response.";
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        Object content = message == null ? null : message.get("content");
        return content == null ? "The assistant returned an empty response." : content.toString().trim();
    }
}
