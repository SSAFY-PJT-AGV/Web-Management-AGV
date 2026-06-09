<template>
  <div
    class="
      terminal-log
      custom-scrollbar
      h-40
      overflow-auto
      p-2
      space-y-1
    "
  >

    <!-- Event Log Row -->
    <div
      v-for="event in items"
      :key="getEventKey(event)"
      class="
        log-enter
        grid
        grid-cols-[70px_72px_1fr]
        gap-2
        text-xs
        tracking-[0.04em]
      "
    >

      <!-- Time -->
      <span class="text-slate-500">
        {{ formatTime(event) }}
      </span>


      <!-- Level -->
      <span :class="levelClass(event.level)">
        [{{ event.level }}]
      </span>


      <!-- Message -->
      <span class="text-slate-300">
        {{ event.message }}
      </span>

    </div>


    <!-- Empty State -->
    <div
      v-if="!items || items.length === 0"
      class="
        py-6
        text-center
        text-xs
        tracking-[0.12em]
        text-slate-600
      "
    >
      NO EVENT LOGS
    </div>

  </div>
</template>


<script setup>
defineProps({
  items: {
    type: Array,
    default: () => []
  }
})


function levelClass(level) {

  const classMap = {
    INFO:
      'text-[#38BDF8]',

    WARN:
      'text-[#FACC15]',

    ERROR:
      'text-[#EF4444]',

    CRITICAL:
      'text-[#DC2626] font-black'
  }


  return (
    classMap[level]
    || 'text-slate-300'
  )
}


function formatTime(event) {

  if (event.time) {
    return event.time
  }

  if (!event.createdAt) {
    return '--:--:--'
  }

  return new Date(event.createdAt)
    .toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
}


function getEventKey(event) {

  return [
    event.eventId,
    event.createdAt,
    event.time,
    event.message
  ].filter(Boolean).join('-')
}
</script>