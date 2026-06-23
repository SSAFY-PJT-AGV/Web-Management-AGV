package com.example.ssafy_pjt.backend.feature.scale.service;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvRole;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import com.example.ssafy_pjt.backend.feature.agv.enums.CargoType;
import com.example.ssafy_pjt.backend.feature.agv.repository.AgvRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ScaleTestAdminService {


    private final AgvRepository agvRepository;


    @Transactional
    public void prepare(
            int supplyCount,
            int collectCount
    ) {

        // 기존 테스트 AGV 제거
        agvRepository.deleteByTestModeTrue();


        createAgvs(
                supplyCount,
                AgvRole.SUPPLY
        );


        createAgvs(
                collectCount,
                AgvRole.COLLECT
        );
    }


    private void createAgvs(
            int count,
            AgvRole role
    ) {

        for (int i = 0; i < count; i++) {

            Agv agv = new Agv();

            agv.setRole(role);
            agv.setStatus(AgvStatus.OFFLINE);

            agv.setCargoType(
                    CargoType.NONE
            );

            agv.setTestMode(true);

            agvRepository.save(agv);
        }
    }


    @Transactional
    public void cleanup() {

        agvRepository.deleteByTestModeTrue();

    }
}
