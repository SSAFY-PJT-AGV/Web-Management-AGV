<template>
  <div class="fixed inset-0 z-[999] flex items-center justify-center bg-black/75">
    <section class="w-[1180px] max-h-[90vh] overflow-y-auto border border-[var(--accent)] bg-[var(--panel)] p-5 custom-scrollbar">
      <div class="mb-5 flex items-center justify-between">
        <div>
          <h2 class="text-xl font-black tracking-[0.25em] text-[var(--accent)]">
            AI OPERATOR ANALYSIS
          </h2>
          <p class="text-xs tracking-[0.15em] text-[var(--muted)]">
            DATA → CONTEXT → ACTION
          </p>
        </div>

        <button class="control-button px-4 py-1" @click="$emit('close')">
          CLOSE
        </button>
      </div>

      <div class="grid grid-cols-[280px_1fr] gap-3">
        <article class="panel-frame p-4">
          <div class="panel-title mb-2">ACTION REQUIRED</div>

          <div class="text-4xl font-black" :class="actionClass">
            {{ actionRequired }}
          </div>

          <div class="mt-3 text-xs leading-5 text-[var(--muted)]">
            {{ actionMessage }}
          </div>

          <div class="mt-4 grid grid-cols-2 gap-2 text-center">
            <div class="metric-box">
              <div class="metric-label">TASKS</div>
              <div class="metric-value text-[var(--accent)]">{{ activeTaskEstimate }}</div>
            </div>

            <div class="metric-box">
              <div class="metric-label">MISSIONS</div>
              <div class="metric-value text-[var(--accent)]">{{ activeMissions.length }}</div>
            </div>

            <div class="metric-box">
              <div class="metric-label">OLDEST WAIT</div>
              <div class="metric-value" :class="oldestWaitClass">{{ oldestWaitText }}</div>
            </div>

            <div class="metric-box">
              <div class="metric-label">FAILED</div>
              <div class="metric-value" :class="failedMissions.length > 0 ? 'text-[var(--red)]' : 'text-[var(--green)]'">
                {{ failedMissions.length }}
              </div>
            </div>
          </div>
        </article>

        <article class="panel-frame p-4">
          <div class="panel-title mb-3">CURRENT OPERATOR BRIEF</div>

          <div class="space-y-3 text-sm leading-6 text-slate-300">
            <p>
              {{ currentBrief }}
            </p>

            <div class="grid grid-cols-3 gap-3">
              <div class="brief-card">
                <div class="brief-label">AGV STATE</div>
                <div class="brief-value" :class="agvStateClass">{{ agvStateText }}</div>
              </div>

              <div class="brief-card">
                <div class="brief-label">QUEUE HEALTH</div>
                <div class="brief-value" :class="queueHealthClass">{{ queueHealthText }}</div>
              </div>

              <div class="brief-card">
                <div class="brief-label">INVENTORY</div>
                <div class="brief-value" :class="inventoryHealthClass">{{ inventoryHealthText }}</div>
              </div>
            </div>
          </div>
        </article>
      </div>

      <div class="mt-3 grid grid-cols-2 gap-3">
        <article class="panel-frame p-4">
          <div class="panel-title mb-3">AGV FLOW CONTEXT</div>

          <div
            v-for="a in agvCards"
            :key="a.agvId"
            class="mb-3 border border-[var(--border2)] bg-black/20 p-3 last:mb-0"
          >
            <div class="mb-2 flex items-center justify-between">
              <span class="text-sm font-bold tracking-[0.12em] text-[var(--accent)]">
                AGV{{ String(a.agvId).padStart(2, '0') }} / {{ a.role || '-' }}
              </span>
              <span class="text-xs font-bold" :class="statusClass(a.status)">
                {{ a.status }}
              </span>
            </div>

            <div class="grid grid-cols-[90px_1fr] gap-2 text-xs leading-5">
              <span class="text-[var(--muted)]">CURRENT</span>
              <span class="text-slate-300">{{ a.currentMissionLabel }}</span>

              <span class="text-[var(--muted)]">MISSIONS</span>
              <span class="text-slate-300">
                {{ a.activeCount }} active / {{ a.waitingCount }} waiting
              </span>

              <span class="text-[var(--muted)]">OLDEST WAIT</span>
              <span :class="waitClass(a.oldestWaitSeconds)">
                {{ a.oldestWaitText }}
              </span>
            </div>

            <div class="mt-2 text-[0.68rem] leading-4 text-[var(--dim)]">
              {{ a.comment }}
            </div>
          </div>
        </article>

        <article class="panel-frame p-4">
          <div class="panel-title mb-3">TIME-BASED BOTTLENECK CHECK</div>

          <div
            v-for="m in topWaitingMissions"
            :key="m.missionId"
            class="mb-3 border border-[var(--border2)] bg-black/20 p-3 last:mb-0"
          >
            <div class="mb-1 flex justify-between text-xs">
              <span class="truncate text-[var(--accent)]">
                #{{ m.missionId }} {{ missionLabel(m.missionType) }}
              </span>
              <span :class="waitClass(m.waitSeconds)">
                {{ m.wait }}
              </span>
            </div>

            <div class="mt-1 flex justify-between text-[0.65rem] text-[var(--muted)]">
              <span>AGV{{ m.agvId ?? '-' }}</span>
              <span>{{ m.status }}</span>
            </div>
          </div>

          <div
            v-if="topWaitingMissions.length === 0"
            class="border border-[var(--border2)] bg-black/20 p-4 text-center text-xs tracking-[0.16em] text-[var(--muted)]"
          >
            NO WAITING MISSION
          </div>
        </article>
      </div>

      <div class="mt-3 grid grid-cols-2 gap-3">
        <article class="panel-frame p-4">
          <div class="panel-title mb-3">INVENTORY IMPACT</div>

          <div
            v-for="i in inventoryCards"
            :key="i.key"
            class="mb-4 last:mb-0"
          >
            <div class="mb-1 flex justify-between text-xs">
              <span>{{ i.code }}</span>
              <span :class="i.statusClass">{{ i.risk }}</span>
            </div>

            <div class="h-3 bg-black">
              <div
                class="h-full bg-[var(--green)]"
                :style="{ width: i.availablePercent + '%' }"
              />
            </div>

            <div class="mt-1 flex justify-between text-[0.65rem] text-[var(--muted)]">
              <span>available {{ i.available }}</span>
              <span>min {{ i.min }}</span>
            </div>
          </div>
        </article>

        <article class="panel-frame p-4">
          <div class="panel-title mb-3">RECOMMENDED OPERATOR ACTION</div>

          <div class="space-y-2">
            <div
              v-for="action in operatorActions"
              :key="action"
              class="status-badge text-xs leading-5 text-slate-300"
            >
              {{ action }}
            </div>
          </div>

          <div class="mt-4 border border-[var(--border2)] bg-black/20 p-3">
            <div class="mb-2 text-xs font-bold tracking-[0.14em] text-[var(--accent)]">
              AI ROLE BOUNDARY
            </div>
            <p class="text-xs leading-5 text-[var(--muted)]">
              AI는 Mission 생성, 상태 변경, COMMAND_ASSIGN 생성을 수행하지 않습니다.
              Rule Scheduler가 최종 제어권을 유지하고, AI는 운영 판단 보조만 수행합니다.
            </p>
          </div>
        </article>
      </div>

      <div class="mt-3 grid grid-cols-[1fr_310px] gap-3">
        <article class="panel-frame p-4">
          <div class="panel-title mb-3">AI MESSAGE SUMMARY</div>

          <div
            v-for="r in safeRecommendations"
            :key="r.title + r.targetKey + r.message"
            class="mb-3 border border-[var(--border2)] bg-black/20 p-3 last:mb-0"
          >
            <div class="mb-1 flex justify-between text-xs">
              <span class="font-bold text-[#FACC15]">{{ r.title }}</span>
              <span class="text-[var(--muted)]">{{ r.targetKey || 'FACTORY' }}</span>
            </div>

            <p class="text-xs leading-5 text-slate-300">
              {{ r.message }}
            </p>
          </div>

          <div
            v-if="safeRecommendations.length === 0"
            class="border border-[var(--border2)] bg-black/20 p-4 text-center text-xs tracking-[0.16em] text-[var(--muted)]"
          >
            NO AI RECOMMENDATION
          </div>
        </article>

        <article class="panel-frame p-4">
          <div class="panel-title mb-3">JUDGEMENT RULES</div>

          <div class="grid grid-cols-1 gap-2 text-xs">
            <div class="status-badge text-[var(--green)]">✓ AGV ERROR / OFFLINE FIRST</div>
            <div class="status-badge text-[var(--green)]">✓ ATOMIC FLOW NOT INTERRUPTED</div>
            <div class="status-badge text-[var(--green)]">✓ WAIT TIME BASED BOTTLENECK</div>
            <div class="status-badge text-[var(--green)]">✓ INVENTORY IMPACT CHECK</div>
            <div class="status-badge text-[var(--red)]">× MISSION COUNT ONLY</div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'

defineEmits(['close'])

const props = defineProps({
  now: {
    type: Number,
    default: () => Date.now()
  },
  recommendations: { type: Array, default: () => [] },
  inventories: { type: Array, default: () => [] },
  missions: { type: Array, default: () => [] },
  agvs: { type: Array, default: () => [] }
})

const activeStatuses = ['CREATED', 'ASSIGNED', 'IN_PROGRESS', 'FAILED']
const waitingStatuses = ['CREATED', 'ASSIGNED']

const activeMissions = computed(() =>
  props.missions.filter(m => activeStatuses.includes(m.status))
)

const failedMissions = computed(() =>
  props.missions.filter(m => m.status === 'FAILED')
)

const safeRecommendations = computed(() =>
  Array.isArray(props.recommendations) ? props.recommendations : []
)

const waitingMissions = computed(() =>
  activeMissions.value
    .filter(m => waitingStatuses.includes(m.status))
    .map(m => ({
      ...m,
      waitSeconds: waitSeconds(m.createdAt ?? m.created_at ?? m.assignedAt ?? m.updatedAt),
      wait: formatSeconds(waitSeconds(m.createdAt ?? m.created_at ?? m.assignedAt ?? m.updatedAt))
    }))
    .sort((a, b) => b.waitSeconds - a.waitSeconds)
)

const topWaitingMissions = computed(() =>
  waitingMissions.value.slice(0, 5)
)

const oldestWaitSeconds = computed(() =>
  waitingMissions.value[0]?.waitSeconds ?? 0
)

const oldestWaitText = computed(() =>
  waitingMissions.value.length === 0 ? '0s' : formatSeconds(oldestWaitSeconds.value)
)

const inventoryCards = computed(() =>
  props.inventories.map((i, index) => {
    const current = i.currentQuantity ?? 0
    const reserved = i.reservedQuantity ?? 0
    const min = i.minThreshold ?? 0
    const available = current - reserved
    const maxBase = Math.max(current, reserved, available, min, 1)

    const risk =
      available <= 0
        ? 'SHORTAGE'
        : available <= min
          ? 'LOW'
          : 'NORMAL'

    return {
      key: i.inventoryId ?? index,
      code: i.materialCode,
      current,
      reserved,
      available,
      min,
      risk,
      availablePercent: Math.round((Math.max(available, 0) / maxBase) * 100),
      statusClass:
        risk === 'SHORTAGE'
          ? 'text-[var(--red)]'
          : risk === 'LOW'
            ? 'text-[var(--amber)]'
            : 'text-[var(--green)]'
    }
  })
)

const hasErrorAgv = computed(() =>
  props.agvs.some(a => a.status === 'ERROR')
)

const hasOfflineAgv = computed(() =>
  props.agvs.some(a => a.status === 'OFFLINE')
)

const hasInventoryRisk = computed(() =>
  inventoryCards.value.some(i => i.risk !== 'NORMAL')
)

const actionRequired = computed(() => {
  if (hasErrorAgv.value || failedMissions.value.length > 0 || oldestWaitSeconds.value >= 120) return 'CHECK'
  if (hasOfflineAgv.value || hasInventoryRisk.value || oldestWaitSeconds.value >= 60) return 'WATCH'
  return 'NONE'
})

const actionClass = computed(() => {
  if (actionRequired.value === 'CHECK') return 'text-[var(--red)]'
  if (actionRequired.value === 'WATCH') return 'text-[var(--amber)]'
  return 'text-[var(--green)]'
})

const actionMessage = computed(() => {
  if (actionRequired.value === 'CHECK') return '운영자 확인이 필요한 상태입니다. AGV 상태, 실패 미션, 최장 대기 미션을 우선 확인하세요.'
  if (actionRequired.value === 'WATCH') return '즉시 중단 상태는 아니지만 주의가 필요합니다. 재고와 AGV 연결 상태를 계속 확인하세요.'
  return '현재 즉시 개입할 신호는 없습니다. 미션 개수는 참고 정보이며 병목 판단 기준은 대기 시간입니다.'
})

const currentBrief = computed(() => {
  if (hasErrorAgv.value) return 'ERROR 상태 AGV가 존재합니다. 신규 작업 요청보다 현재 AGV의 마지막 위치와 진행 중 Flow 상태를 먼저 확인해야 합니다.'
  if (failedMissions.value.length > 0) return '실패한 미션이 있습니다. Event Log에서 직전 이벤트와 실패 위치를 확인하는 것이 우선입니다.'
  if (oldestWaitSeconds.value >= 120) return '오래 대기한 미션이 있어 시간 기준 병목이 의심됩니다. 단순 미션 총량이 아니라 최장 대기 미션을 기준으로 확인하세요.'
  if (hasOfflineAgv.value) return '일부 AGV가 OFFLINE입니다. 진행 중인 Atomic Flow가 남아 있으면 중간 개입하지 말고 연결 복구 여부를 먼저 확인하세요.'
  if (hasInventoryRisk.value) return '재고 영향이 있는 자재가 있습니다. 현재 AGV Flow 완료 후 보급 작업 필요성을 확인하세요.'
  return '현재 AGV Flow는 정상 범위에서 진행 중입니다. 대기 미션 수가 있어도 오래 기다린 미션이 없다면 병목으로 판단하지 않습니다.'
})

const agvCards = computed(() =>
  props.agvs.map(a => {
    const missions = activeMissions.value
      .filter(m => String(m.agvId) === String(a.agvId))
      .sort((x, y) => missionOrder(x) - missionOrder(y))

    const current =
      missions.find(m => m.status === 'IN_PROGRESS') ||
      missions.find(m => m.status === 'ASSIGNED') ||
      missions[0]

    const waiting = missions
      .filter(m => waitingStatuses.includes(m.status))
      .map(m => waitSeconds(m.createdAt ?? m.created_at ?? m.assignedAt ?? m.updatedAt))

    const oldest = waiting.length > 0 ? Math.max(...waiting) : 0

    return {
      agvId: a.agvId,
      role: a.role,
      status: a.status ?? 'UNKNOWN',
      activeCount: missions.length,
      waitingCount: waiting.length,
      currentMissionLabel: current ? missionLabel(current.missionType) : '대기 중',
      oldestWaitSeconds: oldest,
      oldestWaitText: formatSeconds(oldest),
      comment: agvComment(a, missions.length, oldest)
    }
  })
)

const activeTaskEstimate = computed(() => {
  const taskIds = new Set(
    props.missions
      .map(m => m.taskId ?? m.productionTaskId ?? m.requestId)
      .filter(v => v !== null && v !== undefined)
  )

  return taskIds.size > 0 ? taskIds.size : '-'
})

const agvStateText = computed(() => {
  if (hasErrorAgv.value) return 'ERROR'
  if (hasOfflineAgv.value) return 'OFFLINE'
  return 'NORMAL'
})

const agvStateClass = computed(() => {
  if (agvStateText.value === 'NORMAL') return 'text-[var(--green)]'
  if (agvStateText.value === 'OFFLINE') return 'text-[var(--amber)]'
  return 'text-[var(--red)]'
})

const queueHealthText = computed(() => {
  if (oldestWaitSeconds.value >= 120) return 'DELAY'
  if (oldestWaitSeconds.value >= 60) return 'WATCH'
  return 'NORMAL'
})

const queueHealthClass = computed(() => {
  if (queueHealthText.value === 'DELAY') return 'text-[var(--red)]'
  if (queueHealthText.value === 'WATCH') return 'text-[var(--amber)]'
  return 'text-[var(--green)]'
})

const inventoryHealthText = computed(() => {
  if (inventoryCards.value.some(i => i.risk === 'SHORTAGE')) return 'SHORTAGE'
  if (inventoryCards.value.some(i => i.risk === 'LOW')) return 'LOW'
  return 'NORMAL'
})

const inventoryHealthClass = computed(() => {
  if (inventoryHealthText.value === 'SHORTAGE') return 'text-[var(--red)]'
  if (inventoryHealthText.value === 'LOW') return 'text-[var(--amber)]'
  return 'text-[var(--green)]'
})

const operatorActions = computed(() => {
  const actions = []

  if (hasErrorAgv.value) actions.push('1. ERROR AGV의 마지막 위치와 진행 중 Mission을 확인합니다.')
  if (failedMissions.value.length > 0) actions.push('2. FAILED Mission의 Event Log와 실패 위치를 확인합니다.')
  if (oldestWaitSeconds.value >= 120) actions.push('3. 최장 대기 Mission을 기준으로 병목 여부를 확인합니다.')
  if (hasOfflineAgv.value) actions.push('4. OFFLINE AGV의 Heartbeat / WebSocket 연결 상태를 확인합니다.')
  if (hasInventoryRisk.value) actions.push('5. 재고 부족 가능성이 있는 자재의 보급 요청을 검토합니다.')

  if (actions.length === 0) {
    actions.push('현재 즉시 조치가 필요한 신호는 없습니다.')
    actions.push('Mission 개수 증가만으로 병목 판단하지 말고 최장 대기 시간을 계속 확인합니다.')
  }

  return actions
})

function agvComment(agv, activeCount, oldestWait) {
  if (agv.status === 'OFFLINE') return '연결이 끊긴 상태입니다. 진행 중 Mission이 있다면 상태 복구 확인이 우선입니다.'
  if (agv.status === 'ERROR') return '오류 상태입니다. 신규 Mission보다 원인 확인이 우선입니다.'
  if (oldestWait >= 120) return '오래 대기한 Mission이 있습니다. 시간 기준 병목 가능성이 있습니다.'
  if (activeCount >= 4) return '미션 수는 많지만 대기 시간이 짧다면 정상 Flow일 수 있습니다.'
  return '현재 작업 흐름에 즉시 개입할 신호는 없습니다.'
}

function statusClass(status) {
  if (status === 'ERROR' || status === 'OFFLINE') return 'text-[var(--red)]'
  if (status === 'WAITING') return 'text-[var(--amber)]'
  return 'text-[var(--green)]'
}

function waitClass(seconds) {
  if (seconds >= 120) return 'text-[var(--red)]'
  if (seconds >= 60) return 'text-[var(--amber)]'
  return 'text-[var(--green)]'
}

const oldestWaitClass = computed(() => waitClass(oldestWaitSeconds.value))

const missionMap = {
  PICK_FROM_STORAGE: '부품 픽업',
  DROP_TO_CONVEYOR: '컨베이어 투입',
  PICK_FROM_CONVEYOR: '컨베이어 회수',
  DROP_TO_FINISHED_BOX_STORAGE: '부품 투입',
  PICK_FROM_FINISHED_BOX_STORAGE: '완제품 상자 픽업',
  PICK_FROM_INBOUND: '부품 상자 픽업',
  DROP_TO_OUTBOUND: '출고 이동',
  PICK_EMPTY_BOX: '빈 상자 픽업',
  DROP_EMPTY_BOX: '빈 상자 보관',
  DROP_TO_CROSS: '교차 구역 전달',
  PICK_FROM_CROSS: '교차 구역 회수',
  DROP_TO_STORAGE: '자재 창고 복귀',
  RETURN_TO_BASE: '복귀',
  WAIT: '대기',
  STOP: '정지',
  RESUME: '재개'
}

function missionLabel(type) {
  return missionMap[type] ?? type ?? '-'
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
.metric-box,
.brief-card {
  border: 1px solid rgba(56, 189, 248, 0.18);
  background: rgba(0, 0, 0, 0.22);
  padding: 9px;
}

.metric-label,
.brief-label {
  color: var(--muted);
  font-size: 0.58rem;
  letter-spacing: 0.14em;
}

.metric-value,
.brief-value {
  margin-top: 5px;
  font-size: 0.85rem;
  font-weight: 900;
  letter-spacing: 0.08em;
}
</style>
