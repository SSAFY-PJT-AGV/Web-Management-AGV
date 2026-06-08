<template><div class="terminal-log custom-scrollbar overflow-auto h-40 p-2 space-y-1"><div v-for="e in items" :key="e.time+e.message" class="log-enter text-xs"><span class="text-slate-500">{{e.time}}</span> <span :class="levelClass(e.level)">[{{e.level}}]</span> <span>{{e.message}}</span></div></div></template>
<script setup>
defineProps({items:Array})
const levelClass=l=>({INFO:'text-[#38BDF8]',WARN:'text-[#FACC15]',ERROR:'text-[#EF4444]',CRITICAL:'text-[#DC2626] font-black'}[l]||'text-slate-300')
</script>
