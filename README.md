# Smart Factory AGV Backend

스마트 팩토리 환경에서 AGV 상태를 실시간 관제하고,
생산 작업 기반 AGV 명령 생성 및 자재 흐름 관리를 담당하는 백엔드 서버입니다.

AGV와 WebSocket 통신을 통해 상태 및 카메라 프레임을 수신하고,
OpenCV 기반 ArUco Marker 인식을 통해 위치 보정 정보를 제공합니다.

---

## 기술 스택

### Backend
- Language: Java
- Framework: Spring Boot
- ORM: Spring Data JPA

### Database
- MySQL

### Communication
- WebSocket
  - AGV ↔ Server 실시간 통신
  - Server → Dashboard 실시간 상태 전달

### Computer Vision
- OpenCV
- ArUco Marker Detection

### Infrastructure
- Docker
- AWS EC2

### 기타 라이브러리
- Lombok
- Swagger(OpenAPI)

---

## 폴더 구조

Domain별로 패키지를 구성하며 내부 구조는 아래 형식을 따릅니다.

```
📁 Domain
 ├── controller : REST API 요청 처리
 ├── service    : 비즈니스 로직 처리
 ├── repository : 데이터 접근 계층
 ├── entity     : JPA Entity
 └── dto        : Request / Response 객체
```

Example:

```
src/main/java/com/example/ssafy_pjt/backend
└─ feature
   ├─ agv
   │  ├─ entity
   │  │  └─ Agv.java
   │  ├─ repository
   │  │  └─ AgvRepository.java
   │  └─ enums
   │     ├─ AgvStatus.java
   │     └─ CargoType.java
   │
   ├─ mission
   │  ├─ entity
   │  │  └─ Mission.java
   │  ├─ repository
   │  │  └─ MissionRepository.java
   │  └─ enums
   │     ├─ MissionType.java
   │     └─ MissionStatus.java
   │
   ├─ task
   │  ├─ entity
   │  │  └─ ProductionTask.java
   │  ├─ repository
   │  │  └─ ProductionTaskRepository.java
   │  └─ enums
   │     ├─ ProductType.java
   │     ├─ TaskType.java
   │     ├─ TaskPriority.java
   │     └─ TaskStatus.java
   │
   ├─ material
   │  ├─ entity
   │  │  ├─ Material.java
   │  │  └─ ProductMaterial.java
   │  └─ repository
   │     ├─ MaterialRepository.java
   │     └─ ProductMaterialRepository.java
   │
   ├─ marker
   │  ├─ entity
   │  │  └─ ArucoMarker.java
   │  ├─ repository
   │  │  └─ ArucoMarkerRepository.java
   │  └─ enums
   │     └─ MarkerType.java
   │
   ├─ zone
   │  ├─ entity
   │  │  └─ Zone.java
   │  ├─ repository
   │  │  └─ ZoneRepository.java
   │  └─ enums
   │     ├─ ZoneType.java
   │     └─ ZoneStatus.java
   │
   ├─ reservation
   │  ├─ entity
   │  │  └─ Reservation.java
   │  ├─ repository
   │  │  └─ ReservationRepository.java
   │  └─ enums
   │     └─ ReservationStatus.java
   │
   ├─ inventory
   │  ├─ entity
   │  │  └─ Inventory.java
   │  ├─ repository
   │  │  └─ InventoryRepository.java
   │  └─ enums
   │     └─ InventoryStatus.java
   │
   ├─ recommendation
   │  ├─ entity
   │  │  └─ Recommendation.java
   │  └─ repository
   │     └─ RecommendationRepository.java
   │
   └─ event
      ├─ entity
      │  └─ EventLog.java
      ├─ repository
      │  └─ EventLogRepository.java
      └─ enums
         ├─ EventType.java
         └─ EventLevel.java
```

---

## 주요 기능

### AGV 실시간 관제
- AGV 상태 수신
- 현재 위치 및 작업 상태 관리
- AGV별 Command 전달

### 생산 작업 관리
- 제품 생산 요청 등록
- BOM 기반 필요 자재 계산
- 생산 진행 상태 관리

### 자재 및 재고 관리
- Material 관리
- Inventory 수량 관리
- 부족 자재 확인

### AGV Command 관리
- 생산 작업 기반 AGV 작업 생성
- AGV별 작업 배정
- 작업 완료/실패 이벤트 처리

### ArUco Marker 인식
- AGV 카메라 Frame 수신
- Java OpenCV 기반 Marker Detection
- Marker ID, 거리, 각도 계산
- AGV 위치 보정 데이터 전달

### AI Recommendation
- 생산 작업 및 재고 상태 분석
- 우선 공급 자재 추천
- 추천 이유 및 우선순위 점수 제공

---

## WebSocket Message

### AGV → Server

AGV는 0.05초 주기로 상태 및 이미지 프레임을 전송합니다.

- status
- event
- current command
- camera frame

---

## 실행 방법(수정 중)

### Docker MySQL 실행

```bash
docker compose up -d
```

### Spring Boot 실행

```bash
./gradlew bootRun
```

---

## 협업 규칙

- 기본 브랜치: main
- 개발 브랜치: develop
- 기능 브랜치: feature/기능명

### Branch Example

```
feature/socket
feature/marker
```



