package com.example.ssafy_pjt.backend.feature.agv.repository;

import com.example.ssafy_pjt.backend.feature.agv.entity.Agv;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.ssafy_pjt.backend.feature.agv.enums.AgvRole;
import java.util.List;

public interface AgvRepository extends JpaRepository<Agv, Integer> {
    List<Agv> findByRoleIn(List<AgvRole> roles);

    List<Agv> findByStatusIn(List<AgvStatus> statuses);
}