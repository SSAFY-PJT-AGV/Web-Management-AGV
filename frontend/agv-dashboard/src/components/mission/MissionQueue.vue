<template>
  <div class="overflow-hidden border border-[var(--border)]">

    <table class="w-full border-collapse">
      <thead>
        <tr class="bg-[var(--panel2)]">
          <th class="px-3 py-1.5 text-left text-[0.62rem] font-normal tracking-[0.14em] text-[var(--muted)]">
            NO
          </th>
          <th class="px-3 py-1.5 text-left text-[0.62rem] font-normal tracking-[0.14em] text-[var(--muted)]">
            JOB
          </th>
          <th class="px-3 py-1.5 text-left text-[0.62rem] font-normal tracking-[0.14em] text-[var(--muted)]">
            UNIT
          </th>
          <th class="px-3 py-1.5 text-left text-[0.62rem] font-normal tracking-[0.14em] text-[var(--muted)]">
            STATUS
          </th>
        </tr>
      </thead>

      <tbody>
        <tr
          v-for="item in items"
          :key="item.order"
          class="border-t border-[rgba(26,40,64,0.55)] hover:bg-[var(--panel2)]"
        >
          <td class="px-3 py-1.5 text-[0.75rem] text-[var(--muted)]">
            #{{ String(item.order).padStart(3, '0') }}
          </td>

          <td class="px-3 py-1.5 text-[0.78rem] tracking-[0.06em] text-[var(--text)]">
            {{ item.jobName }}
          </td>

          <td class="px-3 py-1.5 text-[0.75rem] text-[var(--muted)]">
            {{ item.agv || 'PENDING' }}
          </td>

          <td
            class="px-3 py-1.5 text-[0.75rem] font-bold tracking-[0.08em]"
            :class="statusClass(item.status)"
          >
            {{ item.status }}
          </td>
        </tr>

        <tr v-if="!items || items.length === 0">
          <td
            colspan="4"
            class="px-3 py-6 text-center text-[0.75rem] tracking-[0.12em] text-[var(--dim)]"
          >
            NO MISSIONS QUEUED
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
defineProps({
  items: {
    type: Array,
    default: () => []
  }
})

function statusClass(status) {
  if (status === '진행중' || status === 'IN_PROGRESS') {
    return 'text-[var(--accent)]'
  }

  if (status === '대기중' || status === 'QUEUED' || status === '배정됨') {
    return 'text-[var(--amber)]'
  }

  if (status === '실패' || status === 'FAILED') {
    return 'text-[var(--red)]'
  }

  return 'text-[var(--muted)]'
}
</script>