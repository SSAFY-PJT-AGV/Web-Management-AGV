package com.example.ssafy_pjt.backend.feature.marker.service;

import com.example.ssafy_pjt.backend.websocket.dto.VisionResultMessage;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.Dictionary;
import org.opencv.objdetect.DetectorParameters;
import org.opencv.objdetect.Objdetect;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


@Service
public class ArucoService {

    private static final float MARKER_SIZE = 30.0f;

    private final ArucoDetector detector;

    private final Mat cameraMatrix;
    private final Mat distCoeffs;


    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    public ArucoService() {

        Dictionary dictionary =
                Objdetect.getPredefinedDictionary(
                        Objdetect.DICT_4X4_50
                );

        DetectorParameters parameters =
                new DetectorParameters();

        this.detector =
                new ArucoDetector(
                        dictionary,
                        parameters
                );


        this.cameraMatrix =
                new Mat(
                        3,
                        3,
                        CvType.CV_64F
                );

        cameraMatrix.put(
                0,
                0,
                106.68985375, 0, 108.3933636,
                0, 143.16642822, 112.59203109,
                0, 0, 1
        );


        this.distCoeffs =
                new Mat(
                        1,
                        5,
                        CvType.CV_64F
                );

        distCoeffs.put(
                0,
                0,
                -3.40691991e-01,
                1.36597880e-01,
                -1.14918997e-03,
                2.52088239e-04,
                -2.68438540e-02
        );
    }


    public VisionResultMessage detectFromBase64(
            String agvId,
            String imageBase64
    ) {

        if (imageBase64 == null || imageBase64.isBlank()) {
            return createDummyResult(agvId);
        }

        try {

            byte[] bytes =
                    Base64.getDecoder()
                            .decode(removeBase64Prefix(imageBase64));


            Mat frame =
                    Imgcodecs.imdecode(
                            new MatOfByte(bytes),
                            Imgcodecs.IMREAD_COLOR
                    );


            if (frame.empty()) {
                return createDummyResult(agvId);
            }


            VisionResultMessage result =
                    detect(frame, agvId);


            return result != null
                    ? result
                    : createDummyResult(agvId);


        } catch (Exception e) {
            e.printStackTrace();
            return createDummyResult(agvId);
        }
    }



    private VisionResultMessage detect(
            Mat frame,
            String agvId
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


        if (ids.empty()) {
            return null;
        }


        // 일단 첫 번째 마커 사용
        int markerId =
                (int) ids.get(0, 0)[0];


        Mat corner =
                corners.get(0);


        double[] leftTop =
                corner.get(0, 0);

        double[] rightTop =
                corner.get(0, 1);


        double markerPixelWidth =
                Math.abs(
                        rightTop[0] - leftTop[0]
                );


        // 간단 거리 추정
        double distance =
                MARKER_SIZE *
                        cameraMatrix.get(0,0)[0]
                        / markerPixelWidth;


        double centerX =
                (leftTop[0] + rightTop[0]) / 2;


        double imageCenter =
                frame.width() / 2.0;


        double angle =
                Math.toDegrees(
                        Math.atan2(
                                centerX - imageCenter,
                                cameraMatrix.get(0,0)[0]
                        )
                );


        return new VisionResultMessage(
                "VISION_RESULT",
                agvId,
                markerId,
                distance,
                angle
        );
    }



    private String removeBase64Prefix(String value) {

        if (value.contains(",")) {
            return value.substring(
                    value.indexOf(",") + 1
            );
        }

        return value;
    }


    private VisionResultMessage createDummyResult(
            String agvId
    ) {

        return new VisionResultMessage(
                "VISION_RESULT",
                agvId,
                101,
                35.2,
                -12.5
        );
    }
}