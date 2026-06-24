package com.example.ssafy_pjt.backend.feature.scale.ai;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class GptScaleAnalysisClient {


    private static final String URL =
            "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions";


    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final PromptLoader promptLoader;


    @Value("${gms.api.key}")
    private String apiKey;


    public String analyze(String metric) {

        Map<String, Object> request =
                Map.of(
                        "model",
                        "gpt-5.1",

                        "messages",
                        List.of(
                                Map.of(
                                        "role",
                                        "developer",
                                        "content",
                                        promptLoader.getScalePrompt()
                                ),
                                Map.of(
                                        "role",
                                        "user",
                                        "content",
                                        metric
                                )
                        )
                );

        String response =
                webClientBuilder.build()
                        .post()
                        .uri(URL)
                        .header(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

        return parse(response);
    }


    private String parse(String response) {

        try {
            JsonNode root =
                    objectMapper.readTree(response);

            return root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {
            throw new RuntimeException(
                    "GPT 분석 응답 파싱 실패",
                    e
            );
        }
    }
}