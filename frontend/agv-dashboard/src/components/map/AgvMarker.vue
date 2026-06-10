<template>
  <g :transform="`translate(${x} ${y})`">

    <!-- AGV Body -->
    <circle
      r="17"
      fill="#0B111A"
      stroke-width="4"
      :class="statusClass"
    />

    <!-- AGV Label -->
    <text
      y="5"
      text-anchor="middle"
      fill="#F8FAFC"
      font-size="11"
      font-weight="900"
    >
      {{ displayAgvId }}
    </text>

  </g>
</template>


<script setup>
import { computed } from 'vue'


const props = defineProps({
  agv: {
    type: Object,
    required: true
  },

  x: {
    type: Number,
    required: true
  },

  y: {
    type: Number,
    required: true
  }
})


const displayAgvId = computed(() => {
   `AGV${String(props.agv.agvId).padStart(2, '0')}`
})


const statusClass = computed(() => {

  const statusMap = {
    MOVING:
      'stroke-[#22D3EE] pulse-cyan',

    IDLE:
      'stroke-slate-500',

    WAITING:
      'stroke-[#FACC15]',

    ERROR:
      'stroke-[#EF4444] critical-glow'
  }


  return (
    statusMap[props.agv.status]
    || 'stroke-slate-400'
  )
})
</script>