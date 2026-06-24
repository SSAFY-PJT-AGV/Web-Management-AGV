<template>
  <g>
    <rect
      :x="marker.xPosition - 24"
      :y="marker.yPosition - 16"
      width="48"
      height="32"
      :fill="fill"
      opacity="0.85"
    />

    <text
      :x="marker.xPosition"
      :y="marker.yPosition + 4"
      text-anchor="middle"
      fill="#05080D"
      font-size="10"
      font-weight="900"
    >
      {{ marker.markerId }}
    </text>

    <text
      :x="marker.xPosition"
      :y="marker.yPosition + 36"
      text-anchor="middle"
      fill="#F8FAFC"
      font-size="12"
      font-weight="800"
    >
      {{ marker.zoneName }}
    </text>
  </g>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  marker: {
    type: Object,
    required: true
  }
})

const fill = computed(() => {
  const colorMap = {
    ZONE: '#2563EB',
    LINE: '#64748B',
    MATERIAL_TYPE: '#22C55E',
    MATERIAL_BOX: '#22C55E',
    PRODUCT_TYPE: '#FACC15',
    EMPTY_BOX: '#94A3B8'
  }

  return colorMap[props.marker.markerType] || '#64748B'
})
</script>