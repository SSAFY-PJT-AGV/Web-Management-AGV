package com.example.ssafy_pjt.backend.feature.simulation.controller;

import com.example.ssafy_pjt.backend.feature.simulation.dto.TempResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class TestController {


    @GetMapping("/api/simulation/temp")
    public TempResponse getSimulation() {

        return new TempResponse(
                List.of(

                        new TempResponse.AgvItem(
                                1,
                                100L,
                                "MOVING",
                                "MATERIAL_BOX_STORAGE",
                                "CONVEYOR_START",
                                3,
                                0,

                                new TempResponse.Position(
                                        2.0,
                                        1.0
                                ),

                                new TempResponse.Position(
                                        0.0,
                                        0.0
                                ),

                                LocalDateTime.now().toString(),
                                "CHIP",
                                "OK"
                        ),


                        new TempResponse.AgvItem(
                                2,
                                null,
                                "OFFLINE",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                LocalDateTime.now().toString(),
                                null,
                                "AGV_LOCATION_UNKNOWN"
                        )

                )
        );
    }
}