<template>
  <div class="grid grid-cols-4 gap-2 text-[11px] tracking-[0.22em]">

    <div class="status-box">
      <p class="label">MODE</p>
      <p class="value text-cyan-300">AUTO</p>
    </div>

    <div class="status-box">
      <p class="label">AGV READY</p>
      <p class="value text-green-300">
        {{ readyAgvCount }} / {{ agvs.length }}
      </p>
    </div>

    <div class="status-box">
      <p class="label">MISSION QUEUED</p>
      <p class="value text-yellow-300">
        {{ queuedCount }}
      </p>
    </div>

    <div class="status-box">
      <p class="label">DISPATCHER</p>
      <p
        class="value"
        :class="connected ? 'text-green-300' : 'text-red-300'"
      >
        {{ connected ? 'STANDBY' : 'OFFLINE' }}
      </p>
    </div>

  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  agvs: {
    type: Array,
    default: () => [],
  },
  missions: {
    type: Array,
    default: () => [],
  },
  connected: {
    type: Boolean,
    default: false,
  },
})

const readyAgvCount = computed(() =>
  props.agvs.filter(agv => agv.status === 'IDLE').length
)

const queuedCount = computed(() =>
  props.missions.filter(mission =>
    ['CREATED', 'ASSIGNED', 'IN_PROGRESS'].includes(mission.status)
  ).length
)
</script>

<style scoped>
.status-box {
  border: 1px solid rgba(56, 189, 248, 0.22);
  background: rgba(15, 23, 42, 0.55);
  padding: 10px 12px;
  min-height: 54px;
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
</style>