package com.example.ssafy_pjt.backend.opencv.testcode;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.DetectorParameters;
import org.opencv.objdetect.Dictionary;
import org.opencv.objdetect.Objdetect;

import java.util.ArrayList;
import java.util.List;

public class ArucoImageTest {

    private static final double MARKER_SIZE = 30.0; // mm

    static {
        String path = System.getProperty("user.dir")
                + "/libs/native/opencv_java490.dll";
        System.load(path);
    }

    public static void main(String[] args) {
        String imagePath = "src/main/resources/test_images/agv_marker.jpg";

        Mat frame = Imgcodecs.imread(imagePath);

        if (frame.empty()) {
            System.out.println("이미지 로드 실패: " + imagePath);
            return;
        }

        VisionResult result = detect(frame);

        System.out.println(result.toJson());
    }

    private static VisionResult detect(Mat frame) {
        Mat cameraMatrix = new Mat(3, 3, CvType.CV_64F);
        cameraMatrix.put(0, 0,
                106.68985375, 0, 108.3933636,
                0, 143.16642822, 112.59203109,
                0, 0, 1
        );

        Dictionary dictionary =
                Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50);

        DetectorParameters parameters = new DetectorParameters();
        ArucoDetector detector = new ArucoDetector(dictionary, parameters);

        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

        List<Mat> corners = new ArrayList<>();
        Mat ids = new Mat();

        detector.detectMarkers(gray, corners, ids);

        if (ids.empty() || corners.isEmpty()) {
            return new VisionResult(null, null, null);
        }

        int markerId = (int) ids.get(0, 0)[0];

        Mat corner = corners.get(0);

        double[] topLeft = corner.get(0, 0);
        double[] topRight = corner.get(0, 1);
        double[] bottomRight = corner.get(0, 2);
        double[] bottomLeft = corner.get(0, 3);

        double centerX = (topLeft[0] + topRight[0] + bottomRight[0] + bottomLeft[0]) / 4.0;

        double markerPixelWidth = distance2d(topLeft, topRight);

        double fx = cameraMatrix.get(0, 0)[0];

        double distance = MARKER_SIZE * fx / markerPixelWidth;

        double angle = Math.toDegrees(
                Math.atan2(
                        centerX - frame.width() / 2.0,
                        fx
                )
        );

        return new VisionResult(markerId, distance, angle);
    }

    private static double distance2d(double[] a, double[] b) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    static class VisionResult {
        Integer markerId;
        Double distance;
        Double angle;

        VisionResult(Integer markerId, Double distance, Double angle) {
            this.markerId = markerId;
            this.distance = distance;
            this.angle = angle;
        }

        String toJson() {
            if (markerId == null) {
                return """
                        {
                          "messageType": "VISION_RESULT",
                          "markerId": null,
                          "distance": null,
                          "angle": null
                        }
                        """;
            }

            return """
                    {
                      "messageType": "VISION_RESULT",
                      "markerId": %d,
                      "distance": %.2f,
                      "angle": %.2f
                    }
                    """.formatted(
                    markerId,
                    distance,
                    angle
            );
        }
    }
}