-- Material
INSERT IGNORE INTO material (material_id, material_code, material_name, description)
VALUES
    (1, 'CHIP', '제어 칩', '연산/제어 부품, A'),
    (2, 'SENSOR', '센서', '인식/측정 부품, B'),
    (3, 'BATTERY', '배터리', '전원 부품, C');

-- Product
INSERT IGNORE INTO product (product_id, product_type, product_name)
VALUES
    (1, 'CAR_CONTROL_UNIT', '차량 제어 장치'),
    (2, 'CAMERA_MODULE', '카메라 센서 모듈'),
    (3, 'BATTERY_PACK', '배터리 팩');

-- Product Material / BOM
INSERT IGNORE INTO product_material (id, product_id, material_id, quantity_per_unit)
VALUES
    (1, 1, 1, 2),
    (2, 1, 2, 1),
    (3, 2, 2, 2),
    (4, 2, 1, 1),
    (5, 3, 3, 2),
    (6, 3, 1, 1);

-- Zone
INSERT IGNORE INTO zone (zone_id, zone_name, zone_type, status)
VALUES
    (1, 'CONVEYOR_START', 'CONVEYOR', 'AVAILABLE'),
    (2, 'CONVEYOR_END', 'CONVEYOR', 'AVAILABLE'),
    (3, 'AGV01_START', 'START', 'AVAILABLE'),
    (4, 'AGV02_START', 'START', 'AVAILABLE'),
    (5, 'FINISHED_BOX_STORAGE', 'STORAGE', 'AVAILABLE'),
    (6, 'MATERIAL_BOX_STORAGE', 'STORAGE', 'AVAILABLE'),
    (7, 'INBOUND', 'INBOUND', 'AVAILABLE'),
    (8, 'OUTBOUND', 'OUTBOUND', 'AVAILABLE'),
    (9, 'CROSS_ZONE', 'CROSS', 'AVAILABLE');

-- AGV
INSERT IGNORE INTO agv (
    agv_id,
    status,
    role,
    current_marker_id,
    current_mission_id,
    cargo_type,
    cargo_material_id,
    last_seen_at
)
VALUES
    (1, 'OFFLINE', 'SUPPLY', NULL, NULL, 'NONE', NULL, NULL),
    (2, 'OFFLINE', 'COLLECT', NULL, NULL, 'NONE', NULL, NULL);

-- Inventory
INSERT IGNORE INTO inventory (
    inventory_id,
    material_id,
    zone_id,
    current_quantity,
    reserved_quantity,
    min_threshold,
    status,
    updated_at
)
VALUES
    (1, 1, 6, 4, 0, 2, 'NORMAL', NOW()),
    (2, 2, 6, 4, 0, 2, 'NORMAL', NOW()),
    (3, 3, 6, 4, 0, 2, 'NORMAL', NOW());

-- ArUco Marker
INSERT IGNORE INTO aruco_marker (
    marker_id,
    marker_type,
    material_id,
    product_id,
    zone_id,
    x_position,
    y_position,
    description,
    is_active
)
VALUES
    (0, 'MATERIAL_TYPE', 1, NULL, NULL, NULL, NULL, 'CHIP 자재 타입 마커', true),
    (1, 'ZONE', NULL, NULL, 1, 0.0, 0.0, 'CONVEYOR_START 위치 마커', true),

    (3, 'MATERIAL_BOX', 1, NULL, 6, NULL, NULL, 'CHIP 보관 상자 마커', true),
    (4, 'MATERIAL_BOX', 2, NULL, 6, NULL, NULL, 'SENSOR 보관 상자 마커', true),
    (5, 'MATERIAL_BOX', 3, NULL, 6, NULL, NULL, 'BATTERY 보관 상자 마커', true),

    (11, 'PRODUCT_TYPE', NULL, 1, 5, NULL, NULL, '차량 제어 장치 출고 상자 마커', true),
    (12, 'PRODUCT_TYPE', NULL, 2, 5, NULL, NULL, '카메라 센서 모듈 출고 상자 마커', true),
    (13, 'PRODUCT_TYPE', NULL, 3, 5, NULL, NULL, '배터리 팩 출고 상자 마커', true),

    (14, 'ZONE', NULL, NULL, 2, 1.0, 0.0, 'CONVEYOR_END 위치 마커', true),

    (103, 'ZONE', NULL, NULL, 3, 0.0, 1.0, 'AGV01_START 위치 마커', true),
    (104, 'ZONE', NULL, NULL, 4, 1.0, 1.0, 'AGV02_START 위치 마커', true),
    (105, 'ZONE', NULL, NULL, 5, 2.0, 0.0, 'FINISHED_BOX_STORAGE 위치 마커', true),
    (106, 'ZONE', NULL, NULL, 6, 2.0, 1.0, 'MATERIAL_BOX_STORAGE 위치 마커', true),
    (107, 'ZONE', NULL, NULL, 7, 3.0, 0.0, 'INBOUND 위치 마커', true),
    (108, 'ZONE', NULL, NULL, 8, 3.0, 1.0, 'OUTBOUND 위치 마커', true),
    (109, 'ZONE', NULL, NULL, 9, 4.0, 0.5, 'CROSS_ZONE 위치 마커', true),

    (202, 'MATERIAL_TYPE', 2, NULL, NULL, NULL, NULL, 'SENSOR 자재 타입 마커', true),
    (203, 'MATERIAL_TYPE', 3, NULL, NULL, NULL, NULL, 'BATTERY 자재 타입 마커', true);



-- System
INSERT IGNORE INTO system_state
(id, mode, scenario_status, active_line)
VALUES
    (1, 'AUTO', 'READY', 'LINE_A');