<template><div class="factory-map h-full min-h-[390px] border border-[#1E3A4F]"><svg viewBox="0 0 900 850" class="w-full h-full"><path d="M730 770 H210 C80 770 95 620 120 565 C145 510 120 480 120 420 V300 C120 190 210 170 320 170 H650 C790 170 820 300 760 380 C700 460 780 515 800 580 C825 665 790 730 730 770 Z" fill="none" stroke="#111827" stroke-width="30" stroke-linecap="round"/><path d="M805 90 V600" fill="none" stroke="#111827" stroke-width="24"/><ZoneMarker v-for="m in markers" :key="m.markerId" :marker="m"/><AgvMarker v-for="a in agvs" :key="a.agvId" :agv="a" :x="markerById(a.currentMarker)?.x || 30" :y="markerById(a.currentMarker)?.y || 30"/><text x="450" y="40" text-anchor="middle" fill="#38BDF8" font-size="18" font-weight="900">MARKER BASED FACTORY MAP</text></svg></div></template>
<script setup>
import ZoneMarker from './ZoneMarker.vue'; import AgvMarker from './AgvMarker.vue'
defineProps({markers:Array,agvs:Array,markerById:Function})
</script>
