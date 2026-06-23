<template>
  <div class="space-y-3">
    <article class="queue-item border-l-2 p-3" :class="briefBorderClass">
      <div class="flex items-center justify-between">
        <strong class="text-[0.75rem] tracking-[0.14em]" :class="briefTitleClass">
          {{ briefTitle }}
        </strong>

        <span class="text-[0.6rem] tracking-[0.14em] text-[var(--muted)]">
          OPERATOR BRIEF
        </span>
      </div>

      <p class="mt-2 text-[0.74rem] leading-relaxed text-slate-300">
        {{ briefMessage }}
      </p>

      <div class="mt-3 grid grid-cols-3 gap-2 text-center">
        <div class="brief-metric">
          <div class="metric-label">ACTIVE MISSIONS</div>
          <div class="metric-value text-[var(--accent)]">{{ activeMissionCount }}</div>
        </div>

        <div class="brief-metric">
          <div class="metric-label">OLDEST WAIT</div>
          <div class="metric-value" :class="oldestWaitClass">{{ oldestWaitText }}</div>
        </div>

        <div class="brief-metric">
          <div class="metric-label">ACTION</div>
          <div class="metric-value" :class="actionClass">{{ actionLevel }}</div>
        </div>
      </div>
    </article>

    <article
      v-for="item in topRecommendations"
      :key="item.title + item.targetKey + item.message"
      class="queue-item border-l-2 border-[#FACC15] p-3"
    >
      <div class="flex items-center justify-between">
        <strong class="text-[0.72rem] tracking-[0.12em] text-[#FACC15]">
          {{ item.title }}
        </strong>

        <span
          v-if="item.targetKey"
          class="text-[0.58rem] tracking-[0.12em] text-[var(--muted)]"
        >
          {{ item.targetKey }}
        </span>
      </div>

      <p class="mt-2 text-[0.72rem] leading-relaxed text-slate-300">
        {{ item.message }}
      </p>
    </article>

    <div
      v-if="topRecommendations.length === 0"
      class="py-3 text-center text-xs tracking-[0.12em] text-[var(--dim)]"
    >
      AI 판단 대기 중
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
  items: {
    type: Array,
    default: () => []
  },
  agvs: {
    type: Array,
    default: () => []
  },
  missions: {
    type: Array,
    default: () => []
  },
  inventories: {
    type: Array,
    default: () => []
  }
})

const activeMissionStatuses = ['CREATED', 'ASSIGNED', 'IN_PROGRESS', 'FAILED']
const waitingMissionStatuses = ['CREATED', 'ASSIGNED']

const topRecommendations = computed(() =>
  Array.isArray(props.items) ? props.items.slice(0, 3) : []
)

const activeMissionCount = computed(() =>
  props.missions.filter(m => activeMissionStatuses.includes(m.status)).length
)

const waitingMissions = computed(() =>
  props.missions
    .filter(m => waitingMissionStatuses.includes(m.status))
    .map(m => ({
      ...m,
      waitSeconds: waitSeconds(m.createdAt ?? m.created_at ?? m.assignedAt ?? m.updatedAt)
    }))
    .sort((a, b) => b.waitSeconds - a.waitSeconds)
)

const oldestWaitSeconds = computed(() => waitingMissions.value[0]?.waitSeconds ?? 0)

const oldestWaitText = computed(() =>
  waitingMissions.value.length === 0 ? '0s' : formatSeconds(oldestWaitSeconds.value)
)

const hasOfflineAgv = computed(() =>
  props.agvs.some(a => a.status === 'OFFLINE')
)

const hasErrorAgv = computed(() =>
  props.agvs.some(a => a.status === 'ERROR')
)

const hasFailedMission = computed(() =>
  props.missions.some(m => m.status === 'FAILED')
)

const lowInventories = computed(() =>
  props.inventories.filter(i => {
    const current = i.currentQuantity ?? 0
    const reserved = i.reservedQuantity ?? 0
    const min = i.minThreshold ?? 0
    return current - reserved <= min
  })
)

const actionLevel = computed(() => {
  if (hasErrorAgv.value || hasFailedMission.value || oldestWaitSeconds.value >= 120) return 'CHECK'
  if (hasOfflineAgv.value || lowInventories.value.length > 0 || oldestWaitSeconds.value >= 60) return 'WATCH'
  return 'NONE'
})

const briefTitle = computed(() => {
  if (actionLevel.value === 'CHECK') return '운영자 확인 필요'
  if (actionLevel.value === 'WATCH') return '주의 상태'
  return '정상 운영'
})

const briefMessage = computed(() => {
  if (hasErrorAgv.value) return 'ERROR 상태 AGV가 있습니다. 신규 작업 생성보다 AGV 상태와 마지막 위치를 먼저 확인하세요.'
  if (hasFailedMission.value) return '실패한 미션이 있습니다. 실패 위치와 직전 Event Log를 확인하세요.'
  if (oldestWaitSeconds.value >= 120) return '오래 기다린 미션이 있습니다. 병목은 미션 개수가 아니라 대기 시간 기준으로 판단하세요.'
  if (hasOfflineAgv.value) return '일부 AGV 연결이 끊겼습니다. 진행 중 Flow가 남아 있다면 중간 개입보다 연결 상태 확인이 우선입니다.'
  if (lowInventories.value.length > 0) return `${lowInventories.value.map(i => i.materialCode).join(', ')} 재고가 낮습니다. 현재 Flow 완료 후 보급 필요성을 확인하세요.`
  return 'AGV가 정상 범위에서 작업 중입니다. 대기 미션 수는 참고 지표이며 현재 병목 신호는 없습니다.'
})

const briefBorderClass = computed(() => {
  if (actionLevel.value === 'CHECK') return 'border-[var(--red)]'
  if (actionLevel.value === 'WATCH') return 'border-[var(--amber)]'
  return 'border-[var(--green)]'
})

const briefTitleClass = computed(() => {
  if (actionLevel.value === 'CHECK') return 'text-[var(--red)]'
  if (actionLevel.value === 'WATCH') return 'text-[var(--amber)]'
  return 'text-[var(--green)]'
})

const actionClass = computed(() => {
  if (actionLevel.value === 'CHECK') return 'text-[var(--red)]'
  if (actionLevel.value === 'WATCH') return 'text-[var(--amber)]'
  return 'text-[var(--green)]'
})

const oldestWaitClass = computed(() => {
  if (oldestWaitSeconds.value >= 120) return 'text-[var(--red)]'
  if (oldestWaitSeconds.value >= 60) return 'text-[var(--amber)]'
  return 'text-[var(--green)]'
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
.brief-metric {
  border: 1px solid rgba(56, 189, 248, 0.18);
  background: rgba(0, 0, 0, 0.22);
  padding: 6px;
}

.metric-label {
  color: var(--muted);
  font-size: 0.55rem;
  letter-spacing: 0.12em;
}

.metric-value {
  margin-top: 3px;
  font-size: 0.72rem;
  font-weight: 900;
  letter-spacing: 0.08em;
}
</style>
