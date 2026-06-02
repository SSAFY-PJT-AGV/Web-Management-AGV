package com.example.ssafy_pjt.backend.feature.marker.service;

import com.example.ssafy_pjt.backend.websocket.dto.VisionResultMessage;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Service
public class ArucoService {

    public VisionResultMessage detectFromBase64(String agvId, String imageBase64) {

        try {
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

            System.out.println("이미지 수신 성공");
            System.out.println("AGV ID: " + agvId);
            System.out.println("이미지 크기(byte): " + imageBytes.length);

            String filename = "received_" + agvId + "_" + System.currentTimeMillis() + ".jpg";

            Files.write(Path.of(filename), imageBytes);

            System.out.println("이미지 저장 완료: " + filename);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new VisionResultMessage(
                "VISION_RESULT",
                agvId,
                101,
                35.2,
                -12.5
        );
    }
}