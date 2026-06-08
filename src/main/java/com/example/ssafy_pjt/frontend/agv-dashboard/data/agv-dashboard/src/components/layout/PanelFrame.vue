<template><section class="panel-frame rounded-sm p-3 min-h-0"><div v-if="title" class="flex items-center justify-between mb-2 border-b border-[#1E3A4F] pb-2"><h2 class="text-sm font-black tracking-[.18em] text-[#38BDF8] uppercase">{{ title }}</h2><slot name="action" /></div><slot /></section></template>
<script setup>defineProps({title:String})</script>
