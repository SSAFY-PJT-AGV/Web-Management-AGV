-- Material
INSERT IGNORE INTO material (material_id, material_code, material_name, description)
VALUES
    (1, 'CHIP', '제어 칩', '연산/제어 부품, A'),
    (2, 'SENSOR', '센서', '인식/측정 부품, B');

-- Product
INSERT IGNORE INTO product (product_id, product_type, product_name)
VALUES
    (1, 'CAR_CONTROL_UNIT', '차량 제어 장치'),
    (2, 'CAMERA_MODULE', '카메라 센서 모듈');

-- Product Material / BOM
INSERT IGNORE INTO product_material (id, product_id, material_id, quantity_per_unit)
VALUES
    (1, 1, 1, 1),
    (2, 1, 2, 1),
    (3, 2, 1, 2),
    (4, 2, 2, 1);

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

-- Aruco Marker
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
    -- 물체 식별 마커 (좌표 없음)
    (0, 'MATERIAL_TYPE', 1, NULL, NULL, NULL, NULL, '제어 칩 타입 마커', true),
    (1, 'MATERIAL_TYPE', 2, NULL, NULL, NULL, NULL, '센서 타입 마커', true),

    (2, 'MATERIAL_BOX', 1, NULL, NULL, NULL, NULL, '제어 칩 부품 상자 마커', true),
    (3, 'MATERIAL_BOX', 2, NULL, NULL, NULL, NULL, '센서 부품 상자 마커', true),

    (4, 'PRODUCT_TYPE', NULL, 1, NULL, NULL, NULL, '차량 제어 장치 완제품 상자 마커', true),
    (5, 'PRODUCT_TYPE', NULL, 2, NULL, NULL, NULL, '카메라 센서 모듈 완제품 상자 마커', true),

    (6, 'EMPTY_BOX', NULL, NULL, NULL, NULL, NULL, '빈 완제품 상자 마커', true),


    -- 상단 라인
    (10, 'LINE', NULL, NULL, NULL, 180, 160, '라인트레이싱 마커 1', true),

    (11, 'ZONE', NULL, NULL, 7,
     805, 100,
     '입출고 구역', true),

    (12, 'LINE', NULL, NULL, NULL,
     500, 160,
     '라인트레이싱 마커 2', true),


    -- 오른쪽 라인
    (13, 'LINE', NULL, NULL, NULL,
     805, 300,
     '라인트레이싱 마커 3', true),

    (14, 'ZONE', NULL, NULL, 9,
     805, 480,
     '교차구역', true),

    (15, 'LINE', NULL, NULL, NULL,
     805, 680,
     '라인트레이싱 마커 4', true),


    -- 하단 라인 / AGV01
    (16, 'ZONE', NULL, NULL, 3,
     650, 775,
     'AGV01 시작점', true),

    (17, 'ZONE', NULL, NULL, 6,
     455, 640,
     '자재 보관 구역', true),

    (18, 'LINE', NULL, NULL, NULL,
     500, 775,
     '라인트레이싱 마커 5', true),


    -- 컨베이어
    (19, 'ZONE', NULL, NULL, 1,
     105, 350,
     '컨베이어 입구', true),

    (20, 'ZONE', NULL, NULL, 2,
     105, 500,
     '컨베이어 출구', true),


    -- 완제품 / AGV02
    (21, 'ZONE', NULL, NULL, 5,
     455, 160,
     '완제품 보관 구역 및 AGV02 시작점', true),


    -- 하단 보조 라인
    (22, 'LINE', NULL, NULL, NULL,
     300, 775,
     '라인트레이싱 마커 6', true);
-- System
INSERT IGNORE INTO system_state
(id, mode, scenario_status, active_line)
VALUES
    (1, 'AUTO', 'READY', 'LINE_A');

-- agv
INSERT IGNORE INTO agv (
    agv_id,
    status,
    role,
    current_marker_id,
    current_mission_id,
    cargo_type,
    cargo_material_id,
    last_seen_at,
    test_mode
)
VALUES
(
    1,
    'IDLE',
    'SUPPLY',
    16,
    NULL,
    'NONE',
    NULL,
    NOW(),
    false
),
(
    2,
    'IDLE',
    'COLLECT',
    21,
    NULL,
    'NONE',
    NULL,
    NOW(),
    false
);

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
    (1, 1, 4, 1, 0, 1, 'NORMAL', NOW()),
    (2, 2, 4, 3, 0, 1, 'NORMAL', NOW());


