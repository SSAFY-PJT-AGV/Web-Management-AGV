package com.example.ssafy_pjt.backend.feature.task.service;

import com.example.ssafy_pjt.backend.feature.inventory.entity.Inventory;
import com.example.ssafy_pjt.backend.feature.inventory.repository.InventoryRepository;
import com.example.ssafy_pjt.backend.feature.material.entity.Material;
import com.example.ssafy_pjt.backend.feature.material.entity.ProductMaterial;
import com.example.ssafy_pjt.backend.feature.material.repository.ProductMaterialRepository;
import com.example.ssafy_pjt.backend.feature.mission.entity.Mission;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionStatus;
import com.example.ssafy_pjt.backend.feature.mission.enums.MissionType;
import com.example.ssafy_pjt.backend.feature.mission.repository.MissionRepository;
import com.example.ssafy_pjt.backend.feature.mission.service.MissionDispatchService;
import com.example.ssafy_pjt.backend.feature.mission.service.ReplenishmentService;
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
    private final InventoryRepository inventoryRepository;
    private final ReplenishmentService replenishmentService;

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
                .status(com.example.ssafy_pjt.backend.feature.task.enums.TaskStatus.READY)
                .build();

        ProductionTask savedTask = productionTaskRepository.save(task);

        Product product = productRepository.findByProductType(savedTask.getProductType())
                .orElseThrow(() -> new IllegalArgumentException(
                        "제품 정보를 찾을 수 없습니다. productType=" + savedTask.getProductType()
                ));

        createMissions(savedTask, product);

        checkAndCreateReplenishmentMissions(savedTask);

        missionDispatchService.assignCreatedMissionsToAgvQueues();

        missionDispatchService.dispatchAvailableAgvs();

        dashboardBroadcastService.taskRefresh();
        dashboardBroadcastService.missionRefresh();
        dashboardBroadcastService.inventoryRefresh();

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

    @Transactional
    public void cancelTask(Long taskId) {

        ProductionTask task =
                productionTaskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("존재하지 않는 작업입니다.")
                        );

        task.setStatus(TaskStatus.CANCELLED);

        List<Mission> missions =
                missionRepository.findAll()
                        .stream()
                        .filter(m ->
                                m.getTask() != null &&
                                        m.getTask()
                                                .getTaskId()
                                                .equals(taskId)
                        )
                        .toList();


        for (Mission mission : missions) {

            if (mission.getStatus() == MissionStatus.COMPLETED) {
                continue;
            }

            mission.setStatus(MissionStatus.CANCELLED);

            if (mission.getAgv() != null
                    && mission.getAgv().getCurrentMission() != null
                    && mission.getAgv()
                    .getCurrentMission()
                    .getMissionId()
                    .equals(mission.getMissionId())) {

                mission.getAgv().setCurrentMission(null);
            }
        }

        dashboardBroadcastService.taskRefresh();
        dashboardBroadcastService.missionRefresh();
    }

    private void checkAndCreateReplenishmentMissions(ProductionTask task) {
        List<ProductMaterial> productMaterials =
                productMaterialRepository.findByProduct_ProductType(task.getProductType());

        for (ProductMaterial productMaterial : productMaterials) {
            Material material = productMaterial.getMaterial();

            int requiredQuantity =
                    productMaterial.getQuantityPerUnit() * task.getQuantity();

            Inventory inventory = findInventoryByMaterial(material);

            int available =
                    inventory.getCurrentQuantity()
                            - inventory.getReservedQuantity();

            if (requiredQuantity > available
                    && !hasActiveReplenishmentMission(material)) {

                replenishmentService.createReplenishmentMissions(
                        material.getMaterialCode(),
                        4
                );

                System.out.println(
                        "[TASK SHORTAGE] productType=" + task.getProductType()
                                + ", material=" + material.getMaterialCode()
                                + ", required=" + requiredQuantity
                                + ", available=" + available
                                + ", replenishmentCreated=true"
                );
            }
        }
    }

    private Inventory findInventoryByMaterial(Material material) {
        return inventoryRepository.findAll()
                .stream()
                .filter(inventory -> inventory.getMaterial() != null)
                .filter(inventory ->
                        inventory.getMaterial()
                                .getMaterialId()
                                .equals(material.getMaterialId())
                )
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "재고 정보를 찾을 수 없습니다. material="
                                + material.getMaterialCode()
                ));
    }

    private boolean hasActiveReplenishmentMission(Material material) {
        return missionRepository.findAll()
                .stream()
                .filter(mission -> mission.getMaterial() != null)
                .filter(mission ->
                        mission.getMaterial()
                                .getMaterialId()
                                .equals(material.getMaterialId())
                )
                .filter(mission ->
                        List.of(
                                MissionStatus.CREATED,
                                MissionStatus.ASSIGNED,
                                MissionStatus.IN_PROGRESS
                        ).contains(mission.getStatus())
                )
                .anyMatch(this::isReplenishmentMission);
    }

    private boolean isReplenishmentMission(Mission mission) {
        return List.of(
                MissionType.PICK_FROM_INBOUND,
                MissionType.DROP_TO_CROSS,
                MissionType.PICK_FROM_CROSS,
                MissionType.DROP_TO_STORAGE
        ).contains(mission.getMissionType());
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

        ProductMaterial firstProductMaterial = productMaterials.get(0);

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
                    null,
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
                    null,
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

        // AGV02: 완제품 보관 구역에서 완제품 상자 픽업
        createMission(
                task,
                MissionType.PICK_FROM_FINISHED_BOX_STORAGE,
                firstProductMaterial,
                product,
                task.getQuantity(),
                finishedBoxStorage,
                null,
                sequence++
        );

        // AGV02: 완제품 상자를 출고 구역으로 이동
        createMission(
                task,
                MissionType.DROP_TO_OUTBOUND,
                firstProductMaterial,
                product,
                task.getQuantity(),
                finishedBoxStorage,
                outbound,
                sequence++
        );

        // AGV02: 출고 후 빈 상자 픽업
        createMission(
                task,
                MissionType.PICK_EMPTY_BOX,
                firstProductMaterial,
                product,
                0,
                outbound,
                null,
                sequence++
        );

        // AGV02: 빈 상자를 완제품 보관 구역에 복귀
        createMission(
                task,
                MissionType.DROP_EMPTY_BOX,
                firstProductMaterial,
                product,
                0,
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
        mission.setMaterial(productMaterial.getMaterial());
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