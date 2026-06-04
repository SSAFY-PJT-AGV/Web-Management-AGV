package com.example.ssafy_pjt.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenCVConfig {

    @PostConstruct
    public void loadOpenCV() {
        String path = System.getProperty("user.dir")
                + "/libs/native/opencv_java490.dll";

        System.load(path);

        System.out.println("OpenCV Loaded");
    }
}