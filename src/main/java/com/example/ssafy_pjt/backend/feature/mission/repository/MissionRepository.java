package com.example.ssafy_pjt.backend.feature.mission.repository;

import com.example.ssafy_pjt.backend.feature.agv.enums.AgvRole;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findByStatusOrderByCreatedAtAsc(MissionStatus status);

    List<Mission> findByStatusNotOrderBySequenceOrderAsc(MissionStatus status);

    Optional<Mission> findFirstByMissionTypeInAndStatusOrderByCreatedAtAsc(
            List<MissionType> missionTypes,
            MissionStatus status
    );

    boolean existsByAgv_RoleAndMissionTypeAndStatusIn(
            AgvRole role,
            MissionType missionType,
            List<MissionStatus> statuses
    );

    List<Mission> findByAgv_AgvIdAndStatusOrderBySequenceOrderAsc(
            Integer agvId,
            MissionStatus status
    );

    long countByAgv_AgvIdAndStatusIn(
            Integer agvId,
            List<MissionStatus> statuses
    );

    @Query("select max(m.sequenceOrder) from Mission m")
    Optional<Integer> findMaxSequenceOrder();
}