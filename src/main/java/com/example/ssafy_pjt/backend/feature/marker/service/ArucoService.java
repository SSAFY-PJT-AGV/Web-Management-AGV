package com.example.ssafy_pjt.backend.feature.marker.service;

import com.example.ssafy_pjt.backend.websocket.dto.ArucoResultMessage;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.ArucoDetector;
import org.opencv.core.MatOfDouble;
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
    private final Mat agv1CameraMatrix;
    private final MatOfDouble agv1DistCoeffs;

    private final Mat agv2CameraMatrix;
    private final MatOfDouble agv2DistCoeffs;

    public ArucoService() {
        Dictionary dictionary =
                Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50);

        DetectorParameters parameters =
                new DetectorParameters();

        this.detector =
                new ArucoDetector(dictionary, parameters);

        this.agv1CameraMatrix =
                new Mat(3, 3, CvType.CV_64F);

        this.agv1CameraMatrix.put(
                0,
                0,
                106.68985375, 0, 108.3933636,
                0, 143.16642822, 112.59203109,
                0, 0, 1
        );

        this.agv1DistCoeffs =
                new MatOfDouble(
                        -3.40691991e-01,
                        1.36597880e-01,
                        -1.14918997e-03,
                        2.52088239e-04,
                        -2.68438540e-02
                );


        this.agv2CameraMatrix =
                new Mat(3, 3, CvType.CV_64F);

        this.agv2CameraMatrix.put(
                0,
                0,
                89.68487251563619, 0.0, 113.59654752016765,
                0.0, 117.76347859168304, 111.77344550170994,
                0.0, 0.0, 1.0
        );

        this.agv2DistCoeffs =
                new MatOfDouble(
                        -0.22362460609434323,
                        0.03756858695252534,
                        0.003241252232988784,
                        -6.593849551046396e-06,
                        -0.00246739467879383
                );
    }

    public ArucoResultMessage detectFromBase64(
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
            System.out.println("[ARUCO ERROR] " + e.getMessage());
            return null;
        }
    }

    private Mat getCameraMatrix(Integer agvId) {
        return agvId != null && agvId == 2
                ? agv2CameraMatrix
                : agv1CameraMatrix;
    }

    private MatOfDouble getDistCoeffs(Integer agvId) {
        return agvId != null && agvId == 2
                ? agv2DistCoeffs
                : agv1DistCoeffs;
    }

    private ArucoResultMessage detect(
            Integer agvId,
            Mat frame
    ) {
        int imageWidth = frame.width();
        int imageHeight = frame.height();

        Mat gray = new Mat();

        Imgproc.cvtColor(
                frame,
                gray,
                Imgproc.COLOR_BGR2GRAY
        );

        List<Mat> corners = new ArrayList<>();
        Mat ids = new Mat();

        detector.detectMarkers(
                gray,
                corners,
                ids
        );

        if (ids.empty() || corners.isEmpty()) {
            return buildNotDetectedResult(
                    agvId,
                    imageWidth,
                    imageHeight
            );
        }

        List<ArucoResultMessage.MarkerInfo> markers =
                new ArrayList<>();

        for (int i = 0; i < ids.rows(); i++) {
            int markerId =
                    (int) ids.get(i, 0)[0];

            Mat corner =
                    corners.get(i);

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

            double centerY =
                    (
                            topLeft[1]
                                    + topRight[1]
                                    + bottomRight[1]
                                    + bottomLeft[1]
                    ) / 4.0;

            double xError =
                    (centerX - imageWidth / 2.0)
                            / (imageWidth / 2.0);

            double yError =
                    (centerY - imageHeight / 2.0)
                            / (imageHeight / 2.0);

            double width =
                    distance2d(topLeft, topRight);

            double height =
                    distance2d(topLeft, bottomLeft);

            double area =
                    Imgproc.contourArea(corner);

            Mat rvec =
                    new Mat();

            Mat tvec =
                    new Mat();

            boolean solved =
                    Calib3d.solvePnP(
                            createObjectPoints(),
                            createImagePoints(
                                    topLeft,
                                    topRight,
                                    bottomRight,
                                    bottomLeft
                            ),
                            getCameraMatrix(agvId),
                            getDistCoeffs(agvId),
                            rvec,
                            tvec
                    );

            if (!solved) {
                continue;
            }

            double rx =
                    rvec.get(0, 0)[0];

            double ry =
                    rvec.get(1, 0)[0];

            double rz =
                    rvec.get(2, 0)[0];

            double tx =
                    tvec.get(0, 0)[0];

            double ty =
                    tvec.get(1, 0)[0];

            double tz =
                    tvec.get(2, 0)[0];

            double distance =
                    Math.sqrt(tx * tx + ty * ty + tz * tz);

            double yaw =
                    Math.toDegrees(Math.atan2(tx, tz));

            double pitch =
                    Math.toDegrees(Math.atan2(-ty, tz));

            ArucoResultMessage.MarkerInfo marker =
                    ArucoResultMessage.MarkerInfo.builder()
                            .markerId(markerId)
                            .corners(
                                    ArucoResultMessage.Corners.builder()
                                            .topLeft(point(topLeft))
                                            .topRight(point(topRight))
                                            .bottomRight(point(bottomRight))
                                            .bottomLeft(point(bottomLeft))
                                            .build()
                            )
                            .center(
                                    List.of(
                                            round(centerX),
                                            round(centerY)
                                    )
                            )
                            .xError(round(xError))
                            .yError(round(yError))
                            .rvec(
                                    List.of(
                                            round(rx),
                                            round(ry),
                                            round(rz)
                                    )
                            )
                            .tvec(
                                    List.of(
                                            round(tx),
                                            round(ty),
                                            round(tz)
                                    )
                            )
                            .tx(round(tx))
                            .ty(round(ty))
                            .tz(round(tz))
                            .distance(round(distance))
                            .yaw(round(yaw))
                            .pitch(round(pitch))
                            .width(round(width))
                            .height(round(height))
                            .area(round(area))
                            .xCentered(Math.abs(xError) < 0.05)
                            .yCentered(Math.abs(yError) < 0.05)
                            .centered(
                                    Math.abs(xError) < 0.05
                                            && Math.abs(yError) < 0.05
                            )
                            .build();

            markers.add(marker);
        }

        if (markers.isEmpty()) {
            return buildNotDetectedResult(
                    agvId,
                    imageWidth,
                    imageHeight
            );
        }

        return ArucoResultMessage.builder()
                .messageType("VISION_RESULT")
                .type("aruco")
                .agvId(agvId)
                .detected(true)
                .markerCount(markers.size())
                .imageWidth(imageWidth)
                .imageHeight(imageHeight)
                .markers(markers)
                .timestamp(nowSeconds())
                .build();
    }

    private MatOfPoint3f createObjectPoints() {
        double half =
                MARKER_SIZE / 2.0;

        return new MatOfPoint3f(
                new Point3(-half, -half, 0),
                new Point3(half, -half, 0),
                new Point3(half, half, 0),
                new Point3(-half, half, 0)
        );
    }

    private MatOfPoint2f createImagePoints(
            double[] topLeft,
            double[] topRight,
            double[] bottomRight,
            double[] bottomLeft
    ) {
        return new MatOfPoint2f(
                new Point(topLeft[0], topLeft[1]),
                new Point(topRight[0], topRight[1]),
                new Point(bottomRight[0], bottomRight[1]),
                new Point(bottomLeft[0], bottomLeft[1])
        );
    }

    private ArucoResultMessage buildNotDetectedResult(
            Integer agvId,
            Integer imageWidth,
            Integer imageHeight
    ) {
        return ArucoResultMessage.builder()
                .messageType("VISION_RESULT")
                .type("aruco")
                .agvId(agvId)
                .detected(false)
                .markerCount(0)
                .imageWidth(imageWidth)
                .imageHeight(imageHeight)
                .markers(List.of())
                .timestamp(nowSeconds())
                .build();
    }

    private List<Double> point(
            double[] point
    ) {
        return List.of(
                round(point[0]),
                round(point[1])
        );
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

    private String removeBase64Prefix(
            String imageBase64
    ) {
        if (imageBase64.contains(",")) {
            return imageBase64.substring(
                    imageBase64.indexOf(",") + 1
            );
        }

        return imageBase64;
    }

    private double nowSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }

    private double round(
            double value
    ) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}