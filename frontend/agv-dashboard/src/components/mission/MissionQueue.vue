<template>
  <div class="h-full overflow-hidden border border-[var(--border)]">
    <table class="w-full table-fixed border-collapse">
      <thead>
        <tr class="bg-[var(--panel2)]">
          <th class="w-[58px] px-2 py-1.5 text-left text-[0.62rem] font-normal tracking-[0.14em] text-[var(--muted)]">
            NO
          </th>

          <th class="px-2 py-1.5 text-left text-[0.62rem] font-normal tracking-[0.14em] text-[var(--muted)]">
            MISSION
          </th>

          <th class="w-[92px] px-2 py-1.5 text-left text-[0.62rem] font-normal tracking-[0.14em] text-[var(--muted)]">
            STATUS
          </th>
        </tr>
      </thead>

      <tbody>
        <tr
          v-for="item in displayItems"
          :key="item.missionId ?? item.order"
          class="border-t border-[rgba(26,40,64,0.55)] hover:bg-[var(--panel2)]"
        >
          <td class="px-2 py-1.5 text-[0.72rem] text-[var(--muted)]">
            #{{ String(item.order ?? item.sequenceOrder ?? item.missionId ?? 0).padStart(3, '0') }}
          </td>

          <td class="px-2 py-1.5 text-[0.72rem] tracking-[0.04em] text-[var(--text)]">
            {{ missionLabel(item.missionType) }}
          </td>


          <td
            class="px-2 py-1.5 text-[0.72rem] font-bold tracking-[0.06em]"
            :class="statusClass(displayStatus(item))"
          >
            {{ displayStatus(item) }}
          </td>
        </tr>

        <tr v-if="!displayItems || displayItems.length === 0">
          <td colspan="4" class="px-3 py-6 text-center text-[0.75rem] tracking-[0.12em] text-[var(--dim)]">
            NO ACTIVE MISSIONS
          </td>
        </tr>
      </tbody>
    </table>

    <div class="border-t border-[var(--border)] px-2 py-1.5 text-[0.62rem] leading-4 tracking-[0.08em] text-[var(--dim)]">
      Bottleneck 판단 기준: 미션 개수보다 최장 대기 시간
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
  agvStatus: {
    type: String,
    default: 'OFFLINE'
  }
})

const displayItems = computed(() =>
  props.items
    .map(item => {
      const seconds = waitSeconds(
        item.createdAt ??
        item.created_at ??
        item.assignedAt ??
        item.updatedAt
      )

      const status = displayStatus(item)

      return {
        ...item,
        displayStatus: status,
        waitSeconds: ['CREATED', 'ASSIGNED', 'QUEUED', 'WAITING'].includes(status) ? seconds : 0,
        waitText: ['CREATED', 'ASSIGNED', 'QUEUED', 'WAITING'].includes(status) ? formatSeconds(seconds) : '—'
      }
    })
    .sort((a, b) => {
      const diff = statusOrder(a.displayStatus) - statusOrder(b.displayStatus)

      if (diff !== 0) {
        return diff
      }

      return missionOrder(a) - missionOrder(b)
    })
)

function displayStatus(mission) {
  if (props.agvStatus === 'OFFLINE' && mission.status === 'IN_PROGRESS') {
    return 'WAITING'
  }

  if (mission.status === 'CREATED' && mission.agvId) {
    return 'QUEUED'
  }

  return mission.status
}

function statusOrder(status) {
  const order = {
    IN_PROGRESS: 1,
    ASSIGNED: 2,
    WAITING: 3,
    QUEUED: 4,
    CREATED: 5,
    FAILED: 98
  }

  return order[status] ?? 99
}

function missionOrder(mission) {
  return (
    mission.sequenceOrder ??
    mission.sequence_order ??
    mission.sequence ??
    mission.order ??
    mission.missionId ??
    mission.commandId ??
    999999
  )
}

function statusClass(status) {
  if (status === 'IN_PROGRESS') {
    return 'text-[var(--accent)]'
  }

  if (
    status === 'QUEUED' ||
    status === 'ASSIGNED' ||
    status === 'WAITING' ||
    status === 'CREATED'
  ) {
    return 'text-[var(--amber)]'
  }

  if (status === 'FAILED') {
    return 'text-[var(--red)]'
  }

  return 'text-[var(--muted)]'
}

function waitClass(seconds) {
  if (seconds >= 120) return 'text-[var(--red)]'
  if (seconds >= 60) return 'text-[var(--amber)]'
  return 'text-[var(--muted)]'
}

function waitSeconds(value) {
  if (!value) return 0

  const time = new Date(value).getTime()

  if (Number.isNaN(time)) return 0

  return Math.max(0, Math.floor((props.now - time) / 1000))
}

function formatSeconds(seconds) {
  if (seconds < 60) return `${seconds}s`

  const minutes = Math.floor(seconds / 60)
  const rest = seconds % 60

  return `${minutes}m ${rest}s`
}

const missionMap = {
  PICK_FROM_STORAGE: '부품 픽업',
  DROP_TO_STORAGE: '자재 보관',
  DROP_TO_CONVEYOR: '컨베이어 투입',
  PICK_FROM_CONVEYOR: '컨베이어 수거',
  DROP_TO_FINISHED_BOX_STORAGE: '완제품 적재',
  PICK_FROM_FINISHED_BOX_STORAGE: '완제품 픽업',
  DROP_TO_OUTBOUND: '출고 구역 이동',
  PICK_EMPTY_BOX: '빈 상자 픽업',
  DROP_EMPTY_BOX: '빈 상자 배치',
  PICK_FROM_INBOUND: '입고 구역 픽업',
  DROP_TO_CROSS: '교차 구역 하역',
  PICK_FROM_CROSS: '교차 구역 픽업',
  RETURN_TO_BASE: '복귀',
  WAIT: '대기',
  STOP: '정지',
  RESUME: '재개'
}

function missionLabel(type) {
  return missionMap[type] ?? type ?? '미션'
}
</script>