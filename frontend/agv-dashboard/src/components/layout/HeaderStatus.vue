<template>
  <header class="relative z-[1] flex h-[58px] border-b-2 border-[var(--accent)] bg-[var(--panel)]">

    <!-- Logo -->
    <div
      class="flex items-center gap-3 bg-[var(--accent)] px-5 text-black"
      style="clip-path: polygon(0 0, calc(100% - 18px) 0, 100% 100%, 0 100%)"
    >
      <div class="text-xl font-black">
        ▣
      </div>

      <div>
        <div class="font-['Barlow_Condensed'] text-[1.05rem] font-bold tracking-[0.1em]">
          AGV FLEET MANAGEMENT SYSTEM
        </div>

        <div class="text-[0.6rem] tracking-[0.16em] text-black/60">
          SSAFY SMARTFACTORY CONTROL - v1.0.0
        </div>
      </div>
    </div>

    <!-- Status Area -->
    <div class="flex flex-1 items-center gap-6 px-6">

      <div class="h-7 w-px bg-[var(--border2)]"></div>

      <HeaderStat label="STATION" value="LINE-A" />

      <div class="h-7 w-px bg-[var(--border2)]"></div>

      <HeaderStat label="MODE" value="AUTO" />

      <div class="h-7 w-px bg-[var(--border2)]"></div>

      <HeaderStat label="ACTIVE" value="2 AGV" />

      <div class="h-7 w-px bg-[var(--border2)]"></div>

      <HeaderStat label="MISSION" value="QUEUED" />

      <div class="ml-auto flex items-center gap-5 text-[0.72rem] tracking-[0.12em] text-[var(--muted)]">

        <div class="flex items-center gap-2">
          <span
            class="h-2 w-2"
            :class="connected ? 'bg-[var(--green)] shadow-[0_0_8px_var(--green)]' : 'bg-[var(--amber)] shadow-[0_0_8px_var(--amber)]'"
            :style="!connected ? 'animation: blink 2s step-end infinite' : ''"
          ></span>

          <span>
            WS: {{ connected ? 'LIVE' : 'MOCK' }}
          </span>
        </div>

        <div class="flex items-center gap-2">
          <span class="h-2 w-2 bg-[var(--green)] shadow-[0_0_8px_var(--green)]"></span>
          <span>{{ timeText }}</span>
        </div>

      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'

defineProps({
  connected: {
    type: Boolean,
    default: false
  }
})

const now = ref(new Date())
let timer = null

const timeText = computed(() => {
  return now.value.toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
})

onMounted(() => {
  timer = setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

const HeaderStat = {
  props: {
    label: String,
    value: String
  },

  template: `
    <div class="flex flex-col leading-tight">
      <span class="text-[0.56rem] tracking-[0.16em] text-[var(--muted)]">
        {{ label }}
      </span>

      <span class="text-[0.78rem] tracking-[0.1em] text-[var(--accent)]">
        {{ value }}
      </span>
    </div>
  `
}
</script>