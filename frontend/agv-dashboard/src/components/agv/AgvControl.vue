<template>
  <div class="space-y-1.5">
    <div
      v-for="agv in agvs"
      :key="agv.agvId"
      class="queue-item grid grid-cols-[76px_82px_1fr] items-center gap-2 px-2 py-1.5"
    >
      <div class="text-[0.82rem] font-bold tracking-[0.08em] text-[var(--text)]">
        {{ displayAgvId(agv.agvId) }}
      </div>

      <div
        class="text-[0.68rem] font-bold tracking-[0.12em]"
        :class="statusClass(agv.status)"
      >
        {{ agv.status || 'IDLE' }}
      </div>

      <div class="grid grid-cols-3 gap-1 text-[0.68rem]">
        <button
          class="control-action border-[#FACC15]"
          @click.stop="pause(agv.agvId)"
        >
          정지
        </button>

        <button
          class="control-action border-[#22C55E]"
          @click.stop="resume(agv.agvId)"
        >
          재개
        </button>

        <button
          class="control-action border-[#EF4444]"
          @click.stop="cancel(agv.agvId)"
        >
          취소
        </button>
      </div>
    </div>

    <div
      v-if="!agvs || agvs.length === 0"
      class="py-3 text-center text-xs tracking-[0.12em] text-[var(--dim)]"
    >
      NO AGV CONNECTED
    </div>
  </div>
</template>

<script setup>
import { agvApi } from '../../api/agvApi'

defineProps({
  agvs: {
    type: Array,
    default: () => []
  }
})

function displayAgvId(id) {
  if (typeof id === 'number') return `AGV${String(id).padStart(2, '0')}`
  if (String(id).startsWith('AGV')) return String(id).replace('AGV-', 'AGV')
  return `AGV${id}`
}

function statusClass(status) {
  switch (status) {
    case 'MOVING':
    case 'LOADING':
    case 'UNLOADING':
    case 'ASSIGNED':
      return 'text-[var(--accent)]'

    case 'IDLE':
      return 'text-[var(--green)]'

    case 'WAITING':
      return 'text-[var(--amber)]'

    case 'ERROR':
    case 'OFFLINE':
      return 'text-[var(--red)]'

    default:
      return 'text-[var(--muted)]'
  }
}

const pause = id =>
  agvApi.pause(id)

const resume = id =>
  agvApi.resume(id)

const cancel = id =>
  agvApi.cancel(id)
</script>

<style scoped>
.control-action {
  border-width: 1px;
  background: #101826;
  padding: 0.25rem 0.35rem;
  text-align: center;
  line-height: 1;
}

.control-action:hover {
  background: rgba(56, 189, 248, 0.12);
}
</style>
