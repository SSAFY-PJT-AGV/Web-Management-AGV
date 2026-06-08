<template><div class="grid grid-cols-2 gap-2"><button v-for="a in agvs" :key="a.agvId" class="queue-item p-2 text-left"><b>{{a.agvId}}</b><div class="grid grid-cols-3 gap-1 mt-2 text-xs"><button @click="pause(a.agvId)" class="bg-[#101826] border border-[#FACC15] p-1">정지</button><button @click="resume(a.agvId)" class="bg-[#101826] border border-[#22C55E] p-1">재개</button><button @click="cancel(a.agvId)" class="bg-[#101826] border border-[#EF4444] p-1">취소</button></div></button></div></template>
<script setup>
import { agvApi } from '../../api/agvApi'
defineProps({agvs:Array})
const pause=id=>agvApi.pause(id); const resume=id=>agvApi.resume(id); const cancel=id=>agvApi.cancel(id)
</script>
