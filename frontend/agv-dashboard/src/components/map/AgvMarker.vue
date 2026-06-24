<template>
  <g :transform="`translate(${safeX} ${safeY})`">

    <circle
      r="18"
      fill="#F8FAFC"
      stroke="#CBD5E1"
      stroke-width="3"
    />

    <text
      y="5"
      text-anchor="middle"
      fill="#0F172A"
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
  agv: Object,
  x: Number,
  y: Number
})

const safeX = computed(() => Number(props.x))
const safeY = computed(() => Number(props.y))

const displayAgvId = computed(() =>
  `A${props.agv.agvId}`
)
</script>