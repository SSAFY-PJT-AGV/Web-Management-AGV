package com.example.ssafy_pjt.backend.feature.system.repository;

import com.example.ssafy_pjt.backend.feature.system.entity.SystemState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemStateRepository
        extends JpaRepository<SystemState, Long> {

    Optional<SystemState> findFirstByOrderByIdAsc();

}
