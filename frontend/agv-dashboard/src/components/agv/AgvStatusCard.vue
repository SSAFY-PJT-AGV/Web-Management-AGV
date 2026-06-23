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

const waitingMissions = computed(() =>
  agvMissions.value
    .filter(m => ['CREATED', 'ASSIGNED'].includes(m.status))
    .map(m => ({
      ...m,
      waitSeconds: waitSeconds(m.createdAt ?? m.created_at ?? m.assignedAt ?? m.updatedAt)
    }))
    .sort((a, b) => b.waitSeconds - a.waitSeconds)
)

const currentWorkLabel = computed(() => {
  if (props.agv.status === 'OFFLINE') return '연결 대기'
  if (!currentMission.value) return '대기 중'
  return missionLabel(currentMission.value.missionType)
})

const currentPosition = computed(() => {
  const marker = props.agv.currentMarkerId ?? props.agv.currentMarker ?? props.agv.marker ?? props.agv.located

  if (props.agv.currentZoneName) return props.agv.currentZoneName
  if (props.agv.currentPosition) return props.agv.currentPosition
  if (marker !== null && marker !== undefined && marker !== '') return `Marker ${marker}`

  return '위치 정보 없음'
})

const destinationLabel = computed(() => {
  const m = currentMission.value
  const destination =
    m?.destinationName ??
    m?.targetZoneName ??
    m?.toZoneName ??
    m?.destination ??
    m?.destinationMarkerId ??
    m?.targetMarkerId ??
    props.agv.destination ??
    props.agv.nextMarker

  if (destination !== null && destination !== undefined && destination !== '') {
    return String(destination).startsWith('Marker') ? destination : `Marker/Zone ${destination}`
  }

  return '목적지 없음'
})

const payloadLabel = computed(() => {
  const cargo =
    props.agv.cargoMaterialCode ??
    props.agv.cargoType ??
    props.agv.cargo ??
    currentMission.value?.materialCode ??
    currentMission.value?.cargo

  if (!cargo || cargo === 'NONE') return 'EMPTY'
  return cargo
})

const remainingMissionCount = computed(() => agvMissions.value.length)

const oldestWait = computed(() => waitingMissions.value[0]?.waitSeconds ?? 0)

const oldestWaitText = computed(() => {
  if (waitingMissions.value.length === 0) return '0s'
  return formatSeconds(oldestWait.value)
})

const queueHealth = computed(() => {
  if (oldestWait.value >= 120) return 'DELAY'
  if (oldestWait.value >= 60) return 'WATCH'
  return 'NORMAL'
})

const queueHealthClass = computed(() => {
  if (queueHealth.value === 'DELAY') return 'text-[var(--red)]'
  if (queueHealth.value === 'WATCH') return 'text-[var(--amber)]'
  return 'text-[var(--green)]'
})

const lastSeenText = computed(() => {
  const raw = props.agv.lastSeenAt ?? props.agv.updatedAt ?? props.agv.timestamp

  if (!raw) return '수신 기록 없음'

  const time = typeof raw === 'number'
    ? raw < 10_000_000_000 ? raw * 1000 : raw
    : new Date(raw).getTime()

  if (Number.isNaN(time)) return '수신 기록 없음'

  return `${formatSeconds(Math.max(0, Math.floor((props.now - time) / 1000)))} ago`
})

const lastSeenClass = computed(() => {
  const raw = props.agv.lastSeenAt ?? props.agv.updatedAt ?? props.agv.timestamp
  if (!raw) return 'text-[var(--muted)]'

  const time = typeof raw === 'number'
    ? raw < 10_000_000_000 ? raw * 1000 : raw
    : new Date(raw).getTime()

  if (Number.isNaN(time)) return 'text-[var(--muted)]'

  const diff = Math.max(0, Math.floor((props.now - time) / 1000))
  if (diff >= 10) return 'text-[var(--red)]'
  if (diff >= 5) return 'text-[var(--amber)]'
  return 'text-[var(--green)]'
})

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
  DROP_TO_CONVEYOR: '컨베이어 시작 구역에 투입',
  PICK_FROM_CONVEYOR: '컨베이어 끝 구역에서 회수',
  DROP_TO_FINISHED_BOX_STORAGE: '완제품 상자에 부품 투입',
  PICK_FROM_FINISHED_BOX_STORAGE: '완제품 상자 픽업',
  PICK_FROM_INBOUND: '입고 구역에서 부품 상자 픽업',
  DROP_TO_OUTBOUND: '출고 구역으로 이동',
  PICK_EMPTY_BOX: '빈 상자 픽업',
  DROP_EMPTY_BOX: '빈 상자 보관',
  DROP_TO_CROSS: '교차 구역 전달',
  PICK_FROM_CROSS: '교차 구역 회수',
  DROP_TO_STORAGE: '자재 창고 복귀',
  RETURN_TO_BASE: '기지 복귀',
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

function waitSeconds(value) {
  if (!value) return 0

  const time = new Date(value).getTime()
  if (Number.isNaN(time)) return 0

  return Math.max(0, Math.floor((props.now - time) / 1000))
}

function formatSeconds(sec) {
  if (sec < 60) return `${sec}s`
  return `${Math.floor(sec / 60)}m ${sec % 60}s`
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
