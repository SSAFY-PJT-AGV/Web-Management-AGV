package com.example.ssafy_pjt.backend.feature.marker;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Base64;

@Service
public class ArucoService {

    public Map<String, Object> detectFromBase64(String agvId, String imageBase64) {

        try {
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

            System.out.println("이미지 수신 성공");
            System.out.println("AGV ID: " + agvId);
            System.out.println("이미지 크기(byte): " + imageBytes.length);

            String filename =
                    "received_" + System.currentTimeMillis() + ".jpg";

            Files.write(
                    Path.of(filename),
                    imageBytes
            );

            System.out.println("이미지 저장 완료: " + filename);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("type", "MARKER_RESULT");
        response.put("agvId", agvId);
        response.put("detected", true);
        response.put("markerCount", 1);

        Map<String, Object> marker = new LinkedHashMap<>();

        marker.put("markerId", 101);
        marker.put("distance", 35.2);
        marker.put("yaw", -12.5);
        marker.put("pitch", 0.0);
        marker.put("centered", true);

        response.put("markers", List.of(marker));

        return response;
    }
}