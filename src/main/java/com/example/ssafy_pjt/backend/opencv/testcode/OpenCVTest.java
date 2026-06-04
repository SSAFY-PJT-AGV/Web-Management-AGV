package com.example.ssafy_pjt.backend.opencv.testcode;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class OpenCVTest {

    static {
        String path =
                System.getProperty("user.dir")
                        + "/libs/native/opencv_java490.dll";

        System.load(path);
    }

    public static void main(String[] args) {
        System.out.println("OpenCV Version: " + Core.VERSION);

        Mat m = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println(m.dump());
    }
}