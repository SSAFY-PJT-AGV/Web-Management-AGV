<template>
  <div class="grid grid-cols-4 gap-2 text-[11px] tracking-[0.18em]">
    <div class="status-box">
      <p class="label">FACTORY STATUS</p>
      <p class="value" :class="factoryStatusClass">
        {{ factoryStatus }}
      </p>
      <p class="hint">{{ factoryStatusMessage }}</p>
    </div>

    <div class="status-box">
      <p class="label">AGV ONLINE</p>
      <p class="value text-green-300">
        {{ onlineAgvCount }} / {{ agvs.length }}
      </p>
      <p class="hint">제어 가능 AGV</p>
    </div>

    <div class="status-box">
      <p class="label">TASK / MISSION</p>
      <p class="value text-cyan-300">
        {{ activeTaskCount }} / {{ activeMissionCount }}
      </p>
      <p class="hint">작업 요청 / 실행 미션</p>
    </div>

    <div class="status-box">
      <p class="label">OLDEST WAIT</p>
      <p class="value" :class="oldestWaitClass">
        {{ oldestWaitText }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  now: {
    type: Number,
    default: () => Date.now()
  },
  agvs: {
    type: Array,
    default: () => [],
  },
  missions: {
    type: Array,
    default: () => [],
  },
  tasks: {
    type: Array,
    default: () => [],
  },
  connected: {
    type: Boolean,
    default: false,
  },
})

const waitingStatuses = ['CREATED', 'ASSIGNED']
const activeMissionStatuses = ['CREATED', 'ASSIGNED', 'IN_PROGRESS', 'FAILED']
const activeTaskStatuses = ['READY', 'RUNNING', 'IN_PROGRESS']

const onlineAgvCount = computed(() =>
  props.agvs.filter(agv => agv.status !== 'OFFLINE').length
)

const activeTaskCount = computed(() =>
  props.tasks.filter(task => activeTaskStatuses.includes(task.status)).length
)

const activeMissionCount = computed(() =>
  props.missions.filter(mission => activeMissionStatuses.includes(mission.status)).length
)

const waitingMissions = computed(() =>
  props.missions
    .filter(mission => waitingStatuses.includes(mission.status))
    .map(mission => ({
      ...mission,
      waitSeconds: waitSeconds(
        mission.createdAt ??
        mission.created_at ??
        mission.assignedAt ??
        mission.updatedAt
      )
    }))
    .sort((a, b) => b.waitSeconds - a.waitSeconds)
)

const oldestWaitSeconds = computed(() =>
  waitingMissions.value[0]?.waitSeconds ?? 0
)

const oldestWaitText = computed(() =>
  waitingMissions.value.length === 0
    ? '0s'
    : formatSeconds(oldestWaitSeconds.value)
)

const hasErrorAgv = computed(() =>
  props.agvs.some(agv => agv.status === 'ERROR')
)

const hasOfflineAgv = computed(() =>
  props.agvs.length > 0 && props.agvs.some(agv => agv.status === 'OFFLINE')
)

const hasFailedMission = computed(() =>
  props.missions.some(mission => mission.status === 'FAILED')
)

const factoryStatus = computed(() => {
  if (!props.connected) return 'OFFLINE'
  if (hasErrorAgv.value || hasFailedMission.value || oldestWaitSeconds.value >= 120) return 'CHECK'
  if (hasOfflineAgv.value || oldestWaitSeconds.value >= 60) return 'WATCH'
  return 'NORMAL'
})

const factoryStatusMessage = computed(() => {
  if (factoryStatus.value === 'OFFLINE') return '대시보드 연결 확인'
  if (factoryStatus.value === 'CHECK') return '운영자 확인 필요'
  if (factoryStatus.value === 'WATCH') return '주의 상태 모니터링'
  return '정상 운영 중'
})


const factoryStatusClass = computed(() => {
  if (factoryStatus.value === 'NORMAL') return 'text-green-300'
  if (factoryStatus.value === 'WATCH') return 'text-yellow-300'
  return 'text-red-300'
})

const oldestWaitClass = computed(() => {
  if (oldestWaitSeconds.value >= 120) return 'text-red-300'
  if (oldestWaitSeconds.value >= 60) return 'text-yellow-300'
  return 'text-green-300'
})

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
.status-box {
  border: 1px solid rgba(56, 189, 248, 0.22);
  background: rgba(15, 23, 42, 0.55);
  padding: 10px 12px;
  min-height: 64px;
}

.label {
  color: #64748b;
  font-size: 9px;
  margin-bottom: 6px;
}

.value {
  font-size: 13px;
  font-weight: 900;
}

.hint {
  margin-top: 4px;
  color: #64748b;
  font-size: 9px;
  letter-spacing: 0.08em;
}
</style>
