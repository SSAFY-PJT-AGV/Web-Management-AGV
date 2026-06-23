<template>
  <div class="fixed inset-0 z-[999] flex items-center justify-center bg-black/75">
    <section class="w-[1180px] max-h-[90vh] overflow-y-auto border border-[var(--accent)] bg-[var(--panel)] p-5 custom-scrollbar">

      <div class="mb-5 flex items-center justify-between">
        <div>
          <h2 class="text-xl font-black tracking-[0.25em] text-[var(--accent)]">
            AI CONTROL CENTER
          </h2>
          <p class="text-xs tracking-[0.15em] text-[var(--muted)]">
            REAL TIME FACTORY DECISION ANALYSIS
          </p>
        </div>

        <button class="control-button px-4 py-1" @click="$emit('close')">
          CLOSE
        </button>
      </div>

      <div class="grid grid-cols-[260px_1fr] gap-3">
        <article class="panel-frame p-4">
          <div class="panel-title mb-2">FACTORY HEALTH</div>

          <div class="text-4xl font-black text-[var(--accent)]">
            {{ factoryHealth }}
          </div>

          <div class="mt-1 text-xs tracking-[0.16em] text-[var(--muted)]">
            / 100 SYSTEM SCORE
          </div>

          <div class="mt-4 h-3 bg-black">
            <div
              class="h-full bg-[var(--accent)]"
              :style="{ width: factoryHealth + '%' }"
            />
          </div>

          <div class="mt-3 text-xs text-[var(--muted)]">
            {{ healthMessage }}
          </div>
        </article>

        <article class="panel-frame p-4">
          <div class="panel-title mb-3">AI RECOMMENDATION SCORE</div>

          <div class="grid grid-cols-3 gap-3">
            <div
              v-for="r in safeRecommendations"
              :key="r.title + r.targetKey + r.message"
              class="border border-[var(--border2)] bg-black/20 p-3"
            >
              <div class="flex justify-between">
                <span class="text-xs text-[var(--muted)]">{{ r.targetKey || 'NONE' }}</span>
                <span class="text-lg font-black text-[var(--accent)]">{{ scoreText(r.priorityScore) }}</span>
              </div>

              <div class="mt-2 text-sm font-bold">{{ r.title }}</div>

              <p class="mt-2 line-clamp-3 text-xs leading-5 text-[var(--muted)]">
                {{ r.message }}
              </p>

              <div class="mt-3 h-2 bg-black">
                <div
                  class="h-full bg-[var(--accent)]"
                  :style="{ width: scorePercent(r.priorityScore) + '%' }"
                />
              </div>
            </div>

            <div
              v-if="safeRecommendations.length === 0"
              class="col-span-3 border border-[var(--border2)] bg-black/20 p-4 text-center text-xs tracking-[0.16em] text-[var(--muted)]"
            >
              NO AI RECOMMENDATION
            </div>
          </div>
        </article>
      </div>

      <div class="mt-3 grid grid-cols-2 gap-3">
        <article class="panel-frame p-4">
          <div class="panel-title mb-3">AGV WORKLOAD BALANCE</div>

          <div
            v-for="a in agvCards"
            :key="a.agvId"
            class="mb-4 last:mb-0"
          >
            <div class="mb-1 flex justify-between text-xs">
              <span class="text-[var(--accent)]">
                AGV{{ String(a.agvId).padStart(2, '0') }} / {{ a.role || '-' }}
              </span>
              <span>{{ a.loadScore }}%</span>
            </div>

            <div class="h-3 bg-black">
              <div
                class="h-full bg-[var(--green)]"
                :style="{ width: a.loadScore + '%' }"
              />
            </div>

            <div class="mt-1 flex justify-between text-[0.68rem] text-[var(--muted)]">
              <span>STATUS {{ a.status }}</span>
              <span>ACTIVE {{ a.activeCount }}</span>
            </div>
          </div>
        </article>

        <article class="panel-frame p-4">
          <div class="panel-title mb-3">MISSION STATUS DISTRIBUTION</div>

          <div
            v-for="s in missionStatusCards"
            :key="s.status"
            class="mb-3 last:mb-0"
          >
            <div class="mb-1 flex justify-between text-xs">
              <span>{{ s.status }}</span>
              <span>{{ s.count }}</span>
            </div>

            <div class="h-3 bg-black">
              <div
                class="h-full bg-[var(--accent)]"
                :style="{ width: s.percent + '%' }"
              />
            </div>
          </div>
        </article>
      </div>

      <div class="mt-3 grid grid-cols-2 gap-3">
        <article class="panel-frame p-4">
          <div class="panel-title mb-3">INVENTORY FORECAST</div>

          <div
            v-for="i in inventoryCards"
            :key="i.key"
            class="mb-4 last:mb-0"
          >
            <div class="mb-1 flex justify-between text-xs">
              <span>{{ i.code }}</span>
              <span :class="i.statusClass">{{ i.risk }}</span>
            </div>

            <div class="grid grid-cols-[1fr_52px] items-center gap-3">
              <div>
                <div class="mb-1 h-2 bg-black">
                  <div
                    class="h-full bg-[var(--green)]"
                    :style="{ width: i.currentPercent + '%' }"
                  />
                </div>

                <div class="mb-1 h-2 bg-black">
                  <div
                    class="h-full bg-[var(--amber)]"
                    :style="{ width: i.reservedPercent + '%' }"
                  />
                </div>

                <div class="h-2 bg-black">
                  <div
                    class="h-full bg-[var(--accent)]"
                    :style="{ width: i.availablePercent + '%' }"
                  />
                </div>
              </div>

              <div class="text-right text-[0.65rem] text-[var(--muted)]">
                <div>C {{ i.current }}</div>
                <div>R {{ i.reserved }}</div>
                <div>A {{ i.available }}</div>
              </div>
            </div>

            <div class="mt-1 text-[0.65rem] text-[var(--muted)]">
              MIN {{ i.min }}
            </div>
          </div>
        </article>

        <article class="panel-frame p-4">
          <div class="panel-title mb-3">UPCOMING MISSIONS</div>

          <div
            v-for="m in topWaitingMissions"
            :key="m.missionId"
            class="mb-3 last:mb-0"
          >
            <div class="mb-1 flex justify-between text-xs">
              <span class="truncate text-[var(--accent)]">
                #{{ m.missionId }} {{ missionLabel(m.missionType) }}
              </span>
              <span>{{ m.wait }}</span>
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
            NO ACTIVE MISSION
          </div>
        </article>
      </div>

      <div class="mt-3 grid grid-cols-[1fr_310px] gap-3">
        <article class="panel-frame p-4">
          <div class="panel-title mb-3">AGV QUEUE COMPARISON</div>

          <div class="grid grid-cols-2 gap-3">
            <div
              v-for="a in agvCards"
              :key="'queue-' + a.agvId"
              class="border border-[var(--border2)] bg-black/20 p-3"
            >
              <div class="mb-2 flex justify-between text-xs">
                <span class="text-[var(--accent)]">
                  AGV{{ String(a.agvId).padStart(2, '0') }}
                </span>
                <span>{{ a.activeCount }} MISSIONS</span>
              </div>

              <div class="h-20 flex items-end gap-1">
                <div
                  v-for="n in 5"
                  :key="n"
                  class="flex-1 border border-[var(--border2)]"
                  :class="n <= a.activeCount ? 'bg-[var(--accent)]' : 'bg-black'"
                />
              </div>

              <div class="mt-2 text-xs text-[var(--muted)]">
                {{ a.balanceComment }}
              </div>
            </div>
          </div>
        </article>

        <article class="panel-frame p-4">
          <div class="panel-title mb-3">AI DECISION FACTORS</div>

          <div class="grid grid-cols-1 gap-2 text-xs">
            <div class="status-badge text-[var(--green)]">✓ INVENTORY LEVEL</div>
            <div class="status-badge text-[var(--green)]">✓ AVAILABLE STOCK</div>
            <div class="status-badge text-[var(--green)]">✓ AGV STATUS</div>
            <div class="status-badge text-[var(--green)]">✓ QUEUE BALANCE</div>
            <div class="status-badge text-[var(--green)]">✓ MISSION WAIT TIME</div>
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
  recommendations: { type: Array, default: () => [] },
  inventories: { type: Array, default: () => [] },
  missions: { type: Array, default: () => [] },
  agvs: { type: Array, default: () => [] }
})

const activeMissions = computed(() =>
  props.missions.filter(m =>
    m.status !== 'COMPLETED' &&
    m.status !== 'CANCELLED'
  )
)

const safeRecommendations = computed(() =>
  Array.isArray(props.recommendations) ? props.recommendations : []
)

const agvCards = computed(() =>
  props.agvs.map(a => {
    const activeCount = activeMissions.value.filter(
      m => String(m.agvId) === String(a.agvId)
    ).length

    const loadScore =
      a.status === 'OFFLINE'
        ? 0
        : Math.min(
            100,
            activeCount * 20 + statusLoad(a.status)
          )

    return {
      agvId: a.agvId,
      role: a.role,
      status: a.status,
      activeCount,
      loadScore,
      balanceComment:
        a.status === 'OFFLINE'
          ? 'OFFLINE'
          : a.status === 'ERROR'
            ? 'ERROR'
            : activeCount >= 4
              ? 'HIGH LOAD'
              : activeCount >= 2
                ? 'NORMAL LOAD'
                : 'LOW LOAD'
    }
  })
)

const missionStatusCards = computed(() => {
  const statuses = ['CREATED', 'ASSIGNED', 'IN_PROGRESS', 'FAILED']
  const total = Math.max(1, activeMissions.value.length)

  return statuses.map(status => {
    const count = activeMissions.value.filter(m => m.status === status).length

    return {
      status,
      count,
      percent: Math.round((count / total) * 100)
    }
  })
})

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
      currentPercent: Math.round((current / maxBase) * 100),
      reservedPercent: Math.round((reserved / maxBase) * 100),
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
  return missionMap[type] ?? type
}

const topWaitingMissions = computed(() =>
  activeMissions.value
    .filter(m =>
      m.status === 'CREATED' ||
      m.status === 'ASSIGNED'
    )
    .map(m => {
      const seconds = waitSeconds(m.createdAt)

      return {
        ...m,
        waitSeconds: seconds,
        wait: formatSeconds(seconds)
      }
    })
    .slice(0, 5)
)

const factoryHealth = computed(() => {
  let score = 100

  const hasErrorAgv = props.agvs.some(a => a.status === 'ERROR')
  const hasOfflineAgv = props.agvs.some(a => a.status === 'OFFLINE')
  const hasShortage = inventoryCards.value.some(i => i.risk === 'SHORTAGE')
  const hasFailedMission = activeMissions.value.some(m => m.status === 'FAILED')
  const longWaiting = topWaitingMissions.value.some(m => m.waitSeconds >= 120)

  if (hasErrorAgv) score -= 30
  if (hasOfflineAgv) score -= 20
  if (hasShortage) score -= 30
  if (hasFailedMission) score -= 25
  if (longWaiting) score -= 10

  return Math.max(0, score)
})

const healthMessage = computed(() => {
  if (factoryHealth.value >= 90) return 'SYSTEM NORMAL'
  if (factoryHealth.value >= 70) return 'WATCH REQUIRED'
  if (factoryHealth.value >= 40) return 'OPERATOR CHECK REQUIRED'
  return 'CRITICAL FACTORY STATE'
})

function statusLoad(status) {
  switch (status) {
    case 'IDLE':
      return 0

    case 'ASSIGNED':
      return 20

    case 'MOVING':
    case 'ARRIVED':
      return 30

    case 'LOADING':
    case 'UNLOADING':
      return 40

    case 'WAITING':
      return 50

    case 'ERROR':
    case 'OFFLINE':
      return 0

    default:
      return 0
  }
}

function waitSeconds(createdAt) {
  if (!createdAt) return 0

  const time = new Date(createdAt).getTime()
  if (Number.isNaN(time)) return 0

  return Math.max(0, Math.floor((Date.now() - time) / 1000))
}

function formatSeconds(sec) {
  if (sec < 60) return `${sec}s`
  return `${Math.floor(sec / 60)}m ${sec % 60}s`
}

function scoreText(score) {
  if (score === null || score === undefined) return '-'
  return Math.round(score)
}

function scorePercent(score) {
  if (score === null || score === undefined) return 0
  return Math.min(100, Math.max(0, score))
}
</script>