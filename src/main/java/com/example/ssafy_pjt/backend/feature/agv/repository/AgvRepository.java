package com.example.ssafy_pjt.backend.feature.agv.repository;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvRole;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AgvRepository extends JpaRepository<Agv, Integer> {
    List<Agv> findByRoleIn(List<AgvRole> roles);

    List<Agv> findByStatusIn(List<AgvStatus> statuses);

    List<Agv> findByLastSeenAtBeforeAndStatusNot(
            LocalDateTime timeout,
            AgvStatus status
    );

    @Query("""
        select distinct a
        from Agv a
        left join fetch a.currentMarker cm
        left join fetch cm.zone
        left join fetch a.currentMission m
        left join fetch m.targetZone
        left join fetch a.cargoMaterial
        order by a.agvId asc
        """)
    List<Agv> findAllWithDisplayInfo();
}