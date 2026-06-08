<template><g><rect :x="marker.x-24" :y="marker.y-16" width="48" height="32" :fill="fill" opacity=".85"/><text :x="marker.x" :y="marker.y+36" text-anchor="middle" fill="#F8FAFC" font-size="12" font-weight="800">{{marker.zoneName}}</text><text :x="marker.x" :y="marker.y+4" text-anchor="middle" fill="#05080D" font-size="10" font-weight="900">{{marker.markerId}}</text></g></template>
<script setup>
import { computed } from 'vue'
const p=defineProps({marker:Object})
const fill=computed(()=>({STORAGE:'#2563EB',FINISHED:'#FACC15',CONVEYOR:'#22C55E',CROSS:'#FACC15',START:'#EF4444',INOUT:'#2DD4BF'}[p.marker.type]||'#64748B'))
</script>
