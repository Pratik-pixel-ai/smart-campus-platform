package com.smartcampus.controller;

import com.smartcampus.dto.ai.AiChatRequest;
import com.smartcampus.dto.ai.AiChatResponse;
import com.smartcampus.dto.ai.AiStatusResponse;
import com.smartcampus.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Campus assistant. Every role may ask questions; the answer is built from the
 * caller's own data, which the backend gathers before calling the AI provider.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiService.ask(request));
    }

    @GetMapping("/status")
    public ResponseEntity<AiStatusResponse> status() {
        return ResponseEntity.ok(aiService.status());
    }
}
