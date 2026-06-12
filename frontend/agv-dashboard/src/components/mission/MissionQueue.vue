<template>
  <div class="max-h-[220px] overflow-y-auto overflow-x-hidden border border-[var(--border)]">
    <table class="w-full table-fixed border-collapse">
      <thead>
      <tr class="bg-[var(--panel2)]">
        <th class="w-[42px] px-2 py-1.5 text-left text-[0.62rem] tracking-[0.14em] text-[var(--muted)]">
          NO
        </th>

        <th class="px-2 py-1.5 text-left text-[0.62rem] tracking-[0.14em] text-[var(--muted)]">
          JOB
        </th>

        <th class="w-[110px] px-2 py-1.5 text-left text-[0.62rem] tracking-[0.14em] text-[var(--muted)]">
          STATUS
        </th>
      </tr>
      </thead>

      <tbody>
        <tr
           v-for="(item, index) in items"
            :key="item.missionId"
          class="border-t border-[rgba(26,40,64,0.55)] hover:bg-[var(--panel2)]"
        >
         <td class="px-2 py-1.5 text-[0.75rem] text-[var(--muted)]">
           {{ index + 1 }}
         </td>

         <td class="truncate px-2 py-1.5 text-[0.78rem] tracking-[0.04em] text-[var(--text)]">
           {{ missionLabel(item.missionType) }}
         </td>

         <td
           class="px-2 py-1.5 text-[0.75rem] font-bold tracking-[0.06em]"
           :class="statusClass(missionDisplayStatus(item))"
         >
             {{ missionDisplayStatus(item) }}
         </td>
        </tr>

        <tr v-if="!items || items.length === 0">
          <td colspan="3" class="px-3 py-6 text-center text-[0.75rem] tracking-[0.12em] text-[var(--dim)]">
            NO MISSIONS QUEUED
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
const props = defineProps({
  items: {
    type: Array,
    default: () => []
  },
  agvStatus: {
    type: String,
    default: 'OFFLINE'
  }
})

function missionDisplayStatus(mission) {
  if (props.agvStatus === 'OFFLINE' && mission.status === 'IN_PROGRESS') {
    return 'WAITING'
  }

  if (mission.status === 'CREATED' && mission.agvId) {
    return 'QUEUED'
  }

  return mission.status
}

function statusClass(displayStatus) {
  if (displayStatus === 'IN_PROGRESS') {
    return 'text-[var(--accent)]'
  }

  if (
    displayStatus === 'QUEUED' ||
    displayStatus === 'ASSIGNED' ||
    displayStatus === 'WAITING'
  ) {
    return 'text-[var(--amber)]'
  }

  if (displayStatus === 'FAILED') {
    return 'text-[var(--red)]'
  }

  return 'text-[var(--muted)]'
}

const missionMap = {
  PICK_FROM_STORAGE: '부품 픽업',
  DROP_TO_CONVEYOR: '컨베이어 투입',

  PICK_FROM_CONVEYOR: '컨베이어 회수',
  DROP_TO_FINISHED_BOX_STORAGE: '부품 투입',

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
</script>