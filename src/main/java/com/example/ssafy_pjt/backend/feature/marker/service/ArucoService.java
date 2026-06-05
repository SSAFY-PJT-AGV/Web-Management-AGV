package com.example.ssafy_pjt.backend.feature.marker.service;

import com.example.ssafy_pjt.backend.websocket.dto.VisionResultMessage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.DetectorParameters;
import org.opencv.objdetect.Dictionary;
import org.opencv.objdetect.Objdetect;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class ArucoService {

    private static final double MARKER_SIZE = 30.0; // mm

    private final ArucoDetector detector;
    private final Mat cameraMatrix;

    public ArucoService() {
        Dictionary dictionary =
                Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50);

        DetectorParameters parameters =
                new DetectorParameters();

        this.detector =
                new ArucoDetector(dictionary, parameters);

        this.cameraMatrix =
                new Mat(3, 3, CvType.CV_64F);

        this.cameraMatrix.put(
                0,
                0,
                106.68985375, 0, 108.3933636,
                0, 143.16642822, 112.59203109,
                0, 0, 1
        );
    }

    public VisionResultMessage detectFromBase64(
            Integer agvId,
            String imageBase64
    ) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            return null;
        }

        try {
            String pureBase64 =
                    removeBase64Prefix(imageBase64);

            byte[] imageBytes =
                    Base64.getDecoder().decode(pureBase64);

            Mat frame =
                    Imgcodecs.imdecode(
                            new MatOfByte(imageBytes),
                            Imgcodecs.IMREAD_COLOR
                    );

            if (frame.empty()) {
                return null;
            }

            return detect(agvId, frame);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private VisionResultMessage detect(
            Integer agvId,
            Mat frame
    ) {
        Mat gray = new Mat();

        Imgproc.cvtColor(
                frame,
                gray,
                Imgproc.COLOR_BGR2GRAY
        );

        List<Mat> corners =
                new ArrayList<>();

        Mat ids =
                new Mat();

        detector.detectMarkers(
                gray,
                corners,
                ids
        );

        if (ids.empty() || corners.isEmpty()) {
            return null;
        }

        int bestIndex =
                findLargestMarkerIndex(corners);

        int markerId =
                (int) ids.get(bestIndex, 0)[0];

        Mat corner =
                corners.get(bestIndex);

        double[] topLeft =
                corner.get(0, 0);

        double[] topRight =
                corner.get(0, 1);

        double[] bottomRight =
                corner.get(0, 2);

        double[] bottomLeft =
                corner.get(0, 3);

        double centerX =
                (
                        topLeft[0]
                                + topRight[0]
                                + bottomRight[0]
                                + bottomLeft[0]
                ) / 4.0;

        double markerPixelWidth =
                distance2d(topLeft, topRight);

        if (markerPixelWidth == 0) {
            return null;
        }

        double fx =
                cameraMatrix.get(0, 0)[0];

        double distance =
                MARKER_SIZE * fx / markerPixelWidth;

        double angle =
                Math.toDegrees(
                        Math.atan2(
                                centerX - frame.width() / 2.0,
                                fx
                        )
                );

        return new VisionResultMessage(
                "VISION_RESULT",
                agvId,
                markerId,
                round(distance),
                round(angle)
        );
    }

    private int findLargestMarkerIndex(List<Mat> corners) {
        int bestIndex = 0;
        double bestArea = -1;

        for (int i = 0; i < corners.size(); i++) {
            double area =
                    Imgproc.contourArea(corners.get(i));

            if (area > bestArea) {
                bestArea = area;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    private double distance2d(
            double[] a,
            double[] b
    ) {
        double dx =
                b[0] - a[0];

        double dy =
                b[1] - a[1];

        return Math.sqrt(dx * dx + dy * dy);
    }

    private String removeBase64Prefix(String imageBase64) {
        if (imageBase64.contains(",")) {
            return imageBase64.substring(
                    imageBase64.indexOf(",") + 1
            );
        }

        return imageBase64;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}