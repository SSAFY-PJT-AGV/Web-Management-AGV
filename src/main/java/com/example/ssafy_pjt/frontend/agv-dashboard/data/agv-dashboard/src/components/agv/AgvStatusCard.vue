<template><div class="queue-item p-3"><div class="flex justify-between"><b class="text-lg">{{ agv.agvId }}</b><span class="status-badge" :class="color">{{ agv.status }}</span></div><p class="mt-2 text-sm text-slate-300">{{ agv.currentJob }}</p><div class="mt-3 grid grid-cols-3 gap-2 text-xs"><span>화물<br><b>{{ agv.cargo || '-' }}</b></span><span>Marker<br><b>{{ agv.marker || '-' }}</b></span><span>대기<br><b>{{ agv.waitingMissionCount }}</b></span></div></div></template>
<script setup>
import { computed } from 'vue'
const props=defineProps({agv:Object})
const color=computed(()=>({MOVING:'text-[#22D3EE]',IDLE:'text-slate-400',WAITING:'text-[#FACC15]',ERROR:'text-[#EF4444]'}[props.agv.status]||'text-slate-300'))
</script>
