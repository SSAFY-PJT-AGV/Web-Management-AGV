<template>
  <section class="relative border-b border-[var(--border)] bg-[var(--panel)]">
    <div class="p-4">
      <div class="mb-3 flex items-center justify-between">
        <span class="font-['Barlow_Condensed'] text-3xl font-bold tracking-[0.08em] text-[var(--text)]">
          {{ displayAgvId }}
        </span>

        <span
          class="px-3 py-1 text-[0.72rem] tracking-[0.14em]"
          :class="statusClass"
        >
          {{ displayStatus }}
        </span>
      </div>

      <div class="mb-3 border border-[var(--border2)] bg-black/20 p-2">
        <div class="text-[0.62rem] tracking-[0.16em] text-[var(--muted)]">
          CURRENT WORK
        </div>
        <div class="mt-1 text-[0.82rem] font-bold tracking-[0.06em] text-[var(--accent)]">
          {{ currentWorkLabel }}
        </div>
      </div>

      <div class="space-y-2">
        <div class="grid grid-cols-[82px_1fr] gap-2">
          <span class="field-label">POSITION</span>
          <span class="field-value">{{ currentPosition }}</span>
        </div>

        <div class="grid grid-cols-[82px_1fr] gap-2">
          <span class="field-label">DEST</span>
          <span class="field-value">{{ destinationLabel }}</span>
        </div>

        <div class="grid grid-cols-[82px_1fr] gap-2">
          <span class="field-label">PAYLOAD</span>
          <span class="field-value">{{ payloadLabel }}</span>
        </div>

        <div class="grid grid-cols-[82px_1fr] gap-2">
          <span class="field-label">REMAIN</span>
          <span class="field-value">
            {{ remainingMissionCount }} step{{ remainingMissionCount === 1 ? '' : 's' }}
          </span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  now: {
    type: Number,
    default: () => Date.now()
  },
  agv: {
    type: Object,
    required: true
  },
  missions: {
    type: Array,
    default: () => []
  }
})

const activeStatuses = ['CREATED', 'ASSIGNED', 'IN_PROGRESS', 'FAILED']

const markerLocationMap = {
  10: '라인 이동 중',
  11: '입출고 구역',
  12: '라인 이동 중',
  13: '라인 이동 중',
  14: '교차 구역',
  15: '라인 이동 중',
  16: 'AGV01 시작 위치',
  17: '자재 보관 구역',
  18: '라인 이동 중',
  19: '컨베이어 입구',
  20: '컨베이어 출구',
  21: '완제품 상자 보관 구역',
  22: '라인 이동 중'
}

const destinationMap = {
  11: '입출고 구역',
  14: '교차 구역',
  16: 'AGV01 시작 위치',
  17: '자재 보관 구역',
  19: '컨베이어 입구',
  20: '컨베이어 출구',
  21: '완제품 상자 보관 구역'
}

const cargoMap = {
  NONE: 'EMPTY',
  EMPTY: 'EMPTY',
  EMPTY_BOX: '빈 상자',
  CHIP: '제어 칩',
  SENSOR: '센서',
  CHIP_BOX: '제어 칩 부품 상자',
  SENSOR_BOX: '센서 부품 상자',
  CAR_CONTROL_UNIT: '차량 제어 장치',
  CAMERA_MODULE: '카메라 센서 모듈',
  CAMERA_SENSOR_MODULE: '카메라 센서 모듈'
}

const displayAgvId = computed(() => {
  if (typeof props.agv.agvId === 'number') {
    return `AGV-${String(props.agv.agvId).padStart(2, '0')}`
  }

  if (String(props.agv.agvId).startsWith('AGV')) {
    return String(props.agv.agvId).replace('AGV', 'AGV-')
  }

  return props.agv.agvId
})

const displayStatus = computed(() => props.agv.status || 'IDLE')

const agvMissions = computed(() =>
  props.missions
    .filter(m =>
      String(m.agvId) === String(props.agv.agvId) &&
      activeStatuses.includes(m.status)
    )
    .sort((a, b) => missionOrder(a) - missionOrder(b))
)

const currentMission = computed(() =>
  agvMissions.value.find(m => m.status === 'IN_PROGRESS') ||
  agvMissions.value.find(m => m.status === 'ASSIGNED') ||
  agvMissions.value[0] ||
  null
)

const currentWorkLabel = computed(() => {
  if (props.agv.status === 'OFFLINE') return '연결 대기'
  if (!currentMission.value) return '대기 중'
  return missionLabel(currentMission.value.missionType)
})

const currentPosition = computed(() => {
  if (props.agv.currentLocationName) return props.agv.currentLocationName
  if (props.agv.currentZoneName) return props.agv.currentZoneName
  if (props.agv.currentPositionName) return props.agv.currentPositionName
  if (props.agv.currentPosition) return props.agv.currentPosition

  const marker =
    props.agv.currentMarkerId ??
    props.agv.currentMarker ??
    props.agv.marker ??
    props.agv.located

  return toLocationName(marker)
})

const destinationLabel = computed(() => {
  const m = currentMission.value

  const destinationName =
    m?.destinationName ??
    m?.targetZoneName ??
    m?.toZoneName ??
    props.agv.destinationName ??
    props.agv.targetZoneName

  if (destinationName) return destinationName

  const destination =
    m?.destination ??
    m?.destinationMarkerId ??
    m?.targetMarkerId ??
    props.agv.destination ??
    props.agv.nextMarker

  return toDestinationName(destination)
})

const payloadLabel = computed(() => {
  const cargo =
    props.agv.cargoMaterialCode ??
    props.agv.cargoType ??
    props.agv.cargo ??
    currentMission.value?.materialCode ??
    currentMission.value?.cargo

  if (!cargo || cargo === 'NONE') return 'EMPTY'

  return cargoMap[cargo] ?? cargo
})

const remainingMissionCount = computed(() => agvMissions.value.length)

const statusClass = computed(() => {
  switch (props.agv.status) {
    case 'MOVING':
    case 'LOADING':
    case 'UNLOADING':
    case 'ASSIGNED':
      return 'border border-[var(--accent)] bg-[#001820] text-[var(--accent)]'

    case 'WAITING':
      return 'border border-[var(--amber)] bg-[#1a1000] text-[var(--amber)]'

    case 'ERROR':
      return 'border border-[var(--red)] bg-[#1a0008] text-[var(--red)] critical-glow'

    case 'STOP':
      return 'text-red-400 border-red-400'

    case 'OFFLINE':
      return 'border border-[var(--red)] bg-[#1a0008] text-[var(--red)]'

    default:
      return 'border border-[var(--dim)] bg-[#0a1520] text-[var(--muted)]'
  }
})

const missionMap = {
  PICK_FROM_STORAGE: '자재 보관 구역에서 부품 픽업',
  DROP_TO_CONVEYOR: '컨베이어 입구에 자재 하역',
  PICK_FROM_CONVEYOR: '컨베이어 출구에서 완제품 회수',
  DROP_TO_FINISHED_BOX_STORAGE: '완제품 상자 보관 구역에 적재',
  PICK_FROM_FINISHED_BOX_STORAGE: '완제품 상자 픽업',
  PICK_FROM_INBOUND: '입출고 구역에서 적재',
  DROP_TO_OUTBOUND: '입출고 구역으로 하역',
  PICK_EMPTY_BOX: '빈 상자 픽업',
  DROP_EMPTY_BOX: '빈 상자 하역',
  DROP_TO_CROSS: '교차 구역에 하역',
  PICK_FROM_CROSS: '교차 구역에서 픽업',
  DROP_TO_STORAGE: '자재 보관 구역에 하역',
  RETURN_TO_BASE: '시작 위치 복귀',
  WAIT: '대기',
  STOP: '정지',
  RESUME: '재개'
}

function missionLabel(type) {
  return missionMap[type] ?? type ?? '작업 없음'
}

function missionOrder(mission) {
  return mission.sequenceOrder ?? mission.sequence ?? mission.order ?? mission.missionId ?? 0
}

function toLocationName(marker) {
  if (marker === null || marker === undefined || marker === '') {
    return '위치 정보 없음'
  }

  const id = Number(marker)

  if (Number.isNaN(id)) {
    return String(marker)
  }

  return markerLocationMap[id] ?? '위치 정보 없음'
}

function toDestinationName(marker) {
  if (marker === null || marker === undefined || marker === '') {
    return '목적지 없음'
  }

  const id = Number(marker)

  if (Number.isNaN(id)) {
    return String(marker)
  }

  return destinationMap[id] ?? '목적지 없음'
}
</script>

<style scoped>
.field-label {
  color: var(--muted);
  font-size: 0.65rem;
  letter-spacing: 0.14em;
}

.field-value {
  color: var(--text);
  font-size: 0.78rem;
  letter-spacing: 0.04em;
}
</style>