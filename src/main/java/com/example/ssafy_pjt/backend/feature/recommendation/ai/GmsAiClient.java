package com.example.ssafy_pjt.backend.feature.recommendation.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GmsAiClient {

    private static final String URL =
            "https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent";

    private final WebClient.Builder webClientBuilder;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Value("${gms.api.key}")
    private String apiKey;

    public String analyze(String factoryState) {
        String systemPrompt = loadSystemPrompt();

        String prompt = systemPrompt + "\n\n" + factoryState;

        Map<String, Object> request = Map.of(
                "contents",
                new Object[]{
                        Map.of(
                                "parts",
                                new Object[]{
                                        Map.of("text", prompt)
                                }
                        )
                }
        );

        String response = webClientBuilder.build()
                .post()
                .uri(URL)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractText(response);
    }

    private String extractText(String response) {

        try {
            JsonNode root =
                    objectMapper.readTree(response);

            return root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "AI 응답 파싱 실패",
                    e
            );
        }
    }

    private String loadSystemPrompt() {
        try {
            Resource resource =
                    resourceLoader.getResource("classpath:ai/agv-system-prompt.txt");

            return resource.getContentAsString(StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new IllegalStateException("AI 시스템 프롬프트 파일을 읽을 수 없습니다.", e);
        }
    }
}