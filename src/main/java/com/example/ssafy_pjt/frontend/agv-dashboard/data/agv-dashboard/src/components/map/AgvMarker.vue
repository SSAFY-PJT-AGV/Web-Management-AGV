<template><g :transform="`translate(${x} ${y})`"><circle r="17" :class="klass" fill="#0B111A" stroke-width="4"/><text y="5" text-anchor="middle" fill="#F8FAFC" font-size="11" font-weight="900">{{agv.agvId.replace('AGV','A')}}</text></g></template>
<script setup>
import { computed } from 'vue'
const p=defineProps({agv:Object,x:Number,y:Number})
const klass=computed(()=>({MOVING:'stroke-[#22D3EE] pulse-cyan',IDLE:'stroke-slate-500',WAITING:'stroke-[#FACC15]',ERROR:'stroke-[#EF4444] critical-glow'}[p.agv.status]||'stroke-slate-400'))
</script>
