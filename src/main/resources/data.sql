-- Material
INSERT IGNORE INTO material (material_id, material_code, material_name, description)
VALUES
    (1, 'CHIP', '제어 칩', '연산/제어 부품, A'),
    (2, 'SENSOR', '센서', '인식/측정 부품, B'),
    (3, 'BATTERY', '배터리', '전원 부품, C');

-- Product Material / BOM
INSERT IGNORE INTO product_material (id, product_type, material_id, quantity_per_unit)
VALUES
    (1, 'CAR_CONTROL_UNIT', 1, 2),
    (2, 'CAR_CONTROL_UNIT', 2, 1),
    (3, 'CAMERA_MODULE', 2, 2),
    (4, 'CAMERA_MODULE', 1, 1),
    (5, 'BATTERY_PACK', 3, 2),
    (6, 'BATTERY_PACK', 1, 1);

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
    current_marker_id,
    current_mission_id,
    cargo_type,
    cargo_material_id,
    last_seen_at
)
VALUES
    (1, 'IDLE', NULL, NULL, 'NONE', NULL, NULL),
    (2, 'IDLE', NULL, NULL, 'NONE', NULL, NULL);

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
    (1, 1, 6, 5, 0, 2, 'NORMAL', NOW()),
    (2, 2, 6, 3, 0, 2, 'NORMAL', NOW()),
    (3, 3, 6, 3, 0, 2, 'NORMAL', NOW());

-- ArUco Marker
INSERT IGNORE INTO aruco_marker (
    marker_id,
    marker_type,
    material_id,
    zone_id,
    x_position,
    y_position,
    description,
    is_active
)
VALUES
    (101, 'ZONE', NULL, 1, 0.0, 0.0, 'CONVEYOR_START 위치 마커', true),
    (102, 'ZONE', NULL, 2, 1.0, 0.0, 'CONVEYOR_END 위치 마커', true),
    (103, 'ZONE', NULL, 3, 0.0, 1.0, 'AGV01_START 위치 마커', true),
    (104, 'ZONE', NULL, 4, 1.0, 1.0, 'AGV02_START 위치 마커', true),
    (105, 'ZONE', NULL, 5, 2.0, 0.0, 'FINISHED_BOX_STORAGE 위치 마커', true),
    (106, 'ZONE', NULL, 6, 2.0, 1.0, 'MATERIAL_BOX_STORAGE 위치 마커', true),
    (107, 'ZONE', NULL, 7, 3.0, 0.0, 'INBOUND 위치 마커', true),
    (108, 'ZONE', NULL, 8, 3.0, 1.0, 'OUTBOUND 위치 마커', true),
    (109, 'ZONE', NULL, 9, 4.0, 0.5, 'CROSS_ZONE 위치 마커', true),

    (201, 'MATERIAL_TYPE', 1, NULL, NULL, NULL, 'CHIP 자재 타입 마커', true),
    (202, 'MATERIAL_TYPE', 2, NULL, NULL, NULL, 'SENSOR 자재 타입 마커', true),
    (203, 'MATERIAL_TYPE', 3, NULL, NULL, NULL, 'BATTERY 자재 타입 마커', true),

    (301, 'MATERIAL_BOX', 1, 6, NULL, NULL, 'CHIP 보관 상자 마커', true),
    (302, 'MATERIAL_BOX', 2, 6, NULL, NULL, 'SENSOR 보관 상자 마커', true),
    (303, 'MATERIAL_BOX', 3, 6, NULL, NULL, 'BATTERY 보관 상자 마커', true);