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
          {{ agv.status || 'IDLE' }}
        </span>
      </div>

      <div class="space-y-2">

        <div class="grid grid-cols-[72px_1fr] gap-2">
          <span class="text-[0.65rem] tracking-[0.14em] text-[var(--muted)]">MISSION</span>
          <span class="text-[0.8rem] text-[var(--accent)]">
             {{ agv.currentMissionType || '—' }}
          </span>
        </div>

        <div class="grid grid-cols-[72px_1fr] gap-2">
          <span class="text-[0.65rem] tracking-[0.14em] text-[var(--muted)]">CARGO</span>
          <span class="text-[0.8rem] text-[var(--text)]">
            {{ agv.cargo || '—' }}
          </span>
        </div>

        <div class="grid grid-cols-[72px_1fr] gap-2">
          <span class="text-[0.65rem] tracking-[0.14em] text-[var(--muted)]">TARGET</span>
          <span class="text-[0.8rem] text-[var(--text)]">
            {{ agv.target || '—' }}
          </span>
        </div>
      </div>

      <div class="mt-3 border-t border-[var(--border)] pt-2">
        <div class="mb-1 text-[0.65rem] tracking-[0.14em] text-[var(--muted)]">
          QUEUE — {{ agv.waitingMissionCount ?? 0 }} PENDING
        </div>

        <div
          v-if="(agv.waitingMissionCount ?? 0) > 0"
          class="queue-item px-2 py-1 text-[0.68rem] tracking-[0.08em] text-[var(--text)]"
        >
          WAITING MISSIONS: {{ agv.waitingMissionCount }}
        </div>

        <div
          v-else
          class="text-[0.68rem] tracking-[0.08em] text-[var(--dim)]"
        >
          NO PENDING MISSIONS
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  agv: {
    type: Object,
    required: true
  }
})

const displayAgvId = computed(() => {
  if (typeof props.agv.agvId === 'number') {
    return `AGV-${String(props.agv.agvId).padStart(2, '0')}`
  }

  if (String(props.agv.agvId).startsWith('AGV')) {
    return String(props.agv.agvId).replace('AGV', 'AGV-')
  }

  return props.agv.agvId
})

const statusClass = computed(() => {
  switch (props.agv.status) {
    case 'MOVING':
      return 'border border-[var(--accent)] bg-[#001820] text-[var(--accent)]'

    case 'WAITING':
      return 'border border-[var(--amber)] bg-[#1a1000] text-[var(--amber)]'

    case 'ERROR':
      return 'border border-[var(--red)] bg-[#1a0008] text-[var(--red)] critical-glow'

    case 'STOP':
      return 'text-red-400 border-red-400'

    default:
      return 'border border-[var(--dim)] bg-[#0a1520] text-[var(--muted)]'
  }
})
</script>