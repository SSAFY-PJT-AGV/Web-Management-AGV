<template><div class="space-y-2"><div v-for="r in items" :key="r.title" class="queue-item p-3"><b class="text-[#FACC15]">{{r.title}}</b><p class="text-sm text-slate-300 mt-1">{{r.message}}</p></div></div></template>
<script setup>defineProps({items:Array})</script>
