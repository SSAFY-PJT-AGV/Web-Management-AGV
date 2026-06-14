package com.example.ssafy_pjt.backend.feature.task.service;

import com.example.ssafy_pjt.backend.feature.material.entity.ProductMaterial;
import com.example.ssafy_pjt.backend.feature.material.repository.ProductMaterialRepository;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import com.example.ssafy_pjt.backend.feature.mission.service.MissionDispatchService;
import com.example.ssafy_pjt.backend.feature.product.entity.Product;
import com.example.ssafy_pjt.backend.feature.product.repository.ProductRepository;
import com.example.ssafy_pjt.backend.feature.task.dto.TaskCreateRequest;
import com.example.ssafy_pjt.backend.feature.task.dto.TaskResponse;
import com.example.ssafy_pjt.backend.feature.task.entity.ProductionTask;
import com.example.ssafy_pjt.backend.feature.task.enums.TaskPriority;
import com.example.ssafy_pjt.backend.feature.task.enums.TaskStatus;
import com.example.ssafy_pjt.backend.feature.task.repository.ProductionTaskRepository;
import com.example.ssafy_pjt.backend.feature.zone.entity.Zone;
import com.example.ssafy_pjt.backend.feature.zone.repository.ZoneRepository;
import com.example.ssafy_pjt.backend.websocket.sender.DashboardBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final ProductionTaskRepository productionTaskRepository;
    private final ProductMaterialRepository productMaterialRepository;
    private final ProductRepository productRepository;
    private final MissionRepository missionRepository;
    private final ZoneRepository zoneRepository;
    private final MissionDispatchService missionDispatchService;
    private final DashboardBroadcastService dashboardBroadcastService;

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {

        ProductionTask task = ProductionTask.builder()
                .taskType(request.getTaskType())
                .productType(request.getProductType())
                .quantity(request.getQuantity())
                .priority(
                        request.getPriority() != null
                                ? request.getPriority()
                                : TaskPriority.NORMAL
                )
                .status(TaskStatus.READY)
                .build();

        ProductionTask savedTask = productionTaskRepository.save(task);

        Product product = productRepository.findByProductType(savedTask.getProductType())
                .orElseThrow(() -> new IllegalArgumentException(
                        "제품 정보를 찾을 수 없습니다. productType=" + savedTask.getProductType()
                ));

        createMissions(savedTask, product);

        // 1. 생성된 CREATED Mission들을 AGV별 작업 큐에 전부 배정
        missionDispatchService.assignCreatedMissionsToAgvQueues();

        // 2. 각 AGV 큐의 첫 번째 Mission만 실제 실행 상태로 전환
        missionDispatchService.dispatchAvailableAgvs();

        // 3. 생산 요청 목록 갱신
        dashboardBroadcastService.taskRefresh();

        return new TaskResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks() {
        return productionTaskRepository.findAll()
                .stream()
                .map(TaskResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId) {
        ProductionTask task = productionTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 생산 작업입니다."));

        return new TaskResponse(task);
    }

    private void createMissions(ProductionTask task, Product product) {
        List<ProductMaterial> productMaterials =
                productMaterialRepository.findByProduct_ProductType(task.getProductType());

        Zone materialStorage = zoneRepository.findByZoneName("MATERIAL_BOX_STORAGE")
                .orElseThrow(() -> new IllegalArgumentException("자재 보관 구역이 없습니다."));

        Zone conveyorStart = zoneRepository.findByZoneName("CONVEYOR_START")
                .orElseThrow(() -> new IllegalArgumentException("컨베이어 시작 구역이 없습니다."));

        Zone conveyorEnd = zoneRepository.findByZoneName("CONVEYOR_END")
                .orElseThrow(() -> new IllegalArgumentException("컨베이어 끝 구역이 없습니다."));

        Zone finishedBoxStorage = zoneRepository.findByZoneName("FINISHED_BOX_STORAGE")
                .orElseThrow(() -> new IllegalArgumentException("완제품 상자 보관 구역이 없습니다."));

        Zone outbound = zoneRepository.findByZoneName("OUTBOUND")
                .orElseThrow(() -> new IllegalArgumentException("출고 구역이 없습니다."));

        int sequence = 1;

        for (ProductMaterial productMaterial : productMaterials) {
            int requiredQuantity =
                    productMaterial.getQuantityPerUnit() * task.getQuantity();

            createMission(
                    task,
                    MissionType.PICK_FROM_STORAGE,
                    productMaterial,
                    product,
                    requiredQuantity,
                    materialStorage,
                    materialStorage,
                    sequence++
            );

            createMission(
                    task,
                    MissionType.DROP_TO_CONVEYOR,
                    productMaterial,
                    product,
                    requiredQuantity,
                    materialStorage,
                    conveyorStart,
                    sequence++
            );

            createMission(
                    task,
                    MissionType.PICK_FROM_CONVEYOR,
                    productMaterial,
                    product,
                    requiredQuantity,
                    conveyorEnd,
                    conveyorEnd,
                    sequence++
            );

            createMission(
                    task,
                    MissionType.DROP_TO_FINISHED_BOX_STORAGE,
                    productMaterial,
                    product,
                    requiredQuantity,
                    conveyorEnd,
                    finishedBoxStorage,
                    sequence++
            );
        }

        createMission(
                task,
                MissionType.PICK_FROM_INBOUND,
                null,
                product,
                task.getQuantity(),
                finishedBoxStorage,
                finishedBoxStorage,
                sequence++
        );

        createMission(
                task,
                MissionType.DROP_TO_OUTBOUND,
                null,
                product,
                task.getQuantity(),
                finishedBoxStorage,
                outbound,
                sequence++
        );

        createMission(
                task,
                MissionType.PICK_EMPTY_BOX,
                null,
                product,
                task.getQuantity(),
                outbound,
                outbound,
                sequence++
        );

        createMission(
                task,
                MissionType.DROP_EMPTY_BOX,
                null,
                product,
                task.getQuantity(),
                outbound,
                finishedBoxStorage,
                sequence++
        );
    }

    private void createMission(
            ProductionTask task,
            MissionType missionType,
            ProductMaterial productMaterial,
            Product product,
            int quantity,
            Zone sourceZone,
            Zone targetZone,
            int sequenceOrder
    ) {
        Mission mission = new Mission();

        mission.setTask(task);
        mission.setAgv(null);
        mission.setMissionType(missionType);

        // 자재 기반 Mission 목적지 계산용
        if (productMaterial != null) {
            mission.setMaterial(productMaterial.getMaterial());
        }

        // 완제품 보관 Mission marker 11/12/13 계산용
        mission.setProduct(product);

        mission.setStatus(MissionStatus.CREATED);
        mission.setSequenceOrder(sequenceOrder);
        mission.setQuantity(quantity);
        mission.setSourceZone(sourceZone);
        mission.setTargetZone(targetZone);
        mission.setCreatedAt(LocalDateTime.now());

        missionRepository.save(mission);
    }
}