# AGV Dashboard Frontend 설정

## 실행 방법

### Backend 실행

Spring Boot

주소:
http://localhost:8080


### Frontend 실행

위치 이동

cd frontend/agv-dashboard


패키지 설치

npm install


실행

npm run dev


Vue 주소:

http://localhost:5173


---

# 환경 변수

파일 위치:

frontend/agv-dashboard/.env.development


개발용:

VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws/dashboard


---

# Git 관리

올림:

.env.development
.env.example


올리지 않음:

.env.local
.env.production
node_modules
dist


---

# 배포할 때

EC2 주소는 Git에 올리지 않는다.

예:

VITE_API_BASE_URL=http://EC2주소:8080

이 값은 .env.production 또는 서버 환경변수에서 관리한다.


---

# 구조

Spring Boot

localhost:8080

↓ REST API / WebSocket

Vue

localhost:5173


---

# 주요 API

AGV
GET /api/agvs

Mission
GET /api/missions
GET /api/missions/summary

Inventory
GET /api/inventories

Replenishment
POST /api/replenishments

Task
POST /api/tasks

Map
GET /api/markers/map

Event
GET /api/events