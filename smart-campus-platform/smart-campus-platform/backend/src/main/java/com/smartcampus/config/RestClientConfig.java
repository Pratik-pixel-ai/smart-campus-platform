package com.smartcampus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * HTTP client used by GroqAiService. A timeout is set so a slow AI provider can
 * never hold a request thread open indefinitely.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate groqRestTemplate(RestTemplateBuilder builder,
                                         @Value("${groq.timeout-ms:20000}") long timeoutMs) {
        return builder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }
}
