package com.example.ssafy_pjt.backend.feature.scale.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class PromptLoader {

    private String scalePrompt;


    @PostConstruct
    public void init() {

        try {
            ClassPathResource resource =
                    new ClassPathResource(
                            "ai/scale-analysis-prompt.txt"
                    );

            scalePrompt =
                    new String(
                            resource.getInputStream().readAllBytes(),
                            StandardCharsets.UTF_8
                    );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Scale Prompt 로딩 실패",
                    e
            );
        }
    }


    public String getScalePrompt() {
        return scalePrompt;
    }
}