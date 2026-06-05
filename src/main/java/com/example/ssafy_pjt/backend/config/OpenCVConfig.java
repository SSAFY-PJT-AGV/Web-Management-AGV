package com.example.ssafy_pjt.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class OpenCVConfig {

    @PostConstruct
    public void loadOpenCV() {

        String os =
                System.getProperty("os.name")
                        .toLowerCase();

        if (os.contains("win")) {
            System.load(
                    System.getProperty("user.dir")
                            + "/libs/native/opencv_java490.dll"
            );
        } else {
            System.load(
                    "/usr/lib/jni/libopencv_java4100.so"
            );
        }

        System.out.println("OpenCV loaded");
    }
}