<script setup>
defineProps({
  open: {
    type: Boolean,
    default: false
  },
  analyzing: {
    type: Boolean,
    default: false
  },
  analysis: {
    type: String,
    default: ''
  },
  agvCount: {
    type: Number,
    default: 0
  },
  fps: {
    type: Number,
    default: 0
  },
  duration: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['close'])
</script>

<template>
  <div
    v-if="open"
    class="fixed inset-0 z-[10000] flex items-center justify-center bg-black/70"
  >
    <div class="panel-frame w-[760px] max-h-[82vh] p-5">
      <div class="mb-4 flex items-center justify-between">
        <div>
          <div class="panel-title">
            AI SCALE ANALYSIS REPORT
          </div>

          <div class="mt-1 text-[0.7rem] tracking-[0.12em] text-[var(--muted)]">
            {{ agvCount }} AGV · {{ fps }} FPS · {{ agvCount * fps }} MSG/S
          </div>
        </div>

        <button
          class="control-button px-3 py-1"
          @click="emit('close')"
        >
          CLOSE
        </button>
      </div>

      <div
        v-if="analyzing"
        class="terminal-log border border-[var(--border)] p-8 text-center text-[var(--accent)]"
      >
        <div class="scale-hourglass mx-auto mb-4">
          ⧖
        </div>

        <div class="text-lg tracking-[0.18em]">
          AI 분석 중...
        </div>

        <div class="mt-2 text-[0.68rem] tracking-[0.12em] text-[var(--muted)]">
          성능 지표를 기반으로 병목 위치를 분석하고 있습니다
        </div>
      </div>

      <div
        v-else
        class="grid grid-cols-4 gap-2"
      >
        <div class="border border-[var(--border2)] bg-[var(--panel2)] p-3">
          <div class="text-[0.62rem] tracking-[0.16em] text-[var(--muted)]">
            TARGET LOAD
          </div>

          <div class="mt-2 text-xl text-[var(--accent)]">
            {{ agvCount * fps }}
          </div>

          <div class="text-[0.65rem] text-[var(--muted)]">
            MSG/S
          </div>
        </div>

        <div class="border border-[var(--border2)] bg-[var(--panel2)] p-3">
          <div class="text-[0.62rem] tracking-[0.16em] text-[var(--muted)]">
            AGV COUNT
          </div>

          <div class="mt-2 text-xl text-[var(--text)]">
            {{ agvCount }}
          </div>

          <div class="text-[0.65rem] text-[var(--muted)]">
            TEST UNITS
          </div>
        </div>

        <div class="border border-[var(--border2)] bg-[var(--panel2)] p-3">
          <div class="text-[0.62rem] tracking-[0.16em] text-[var(--muted)]">
            FPS
          </div>

          <div class="mt-2 text-xl text-[var(--green)]">
            {{ fps }}
          </div>

          <div class="text-[0.65rem] text-[var(--muted)]">
            PER AGV
          </div>
        </div>

        <div class="border border-[var(--border2)] bg-[var(--panel2)] p-3">
          <div class="text-[0.62rem] tracking-[0.16em] text-[var(--muted)]">
            DURATION
          </div>

          <div class="mt-2 text-xl text-[var(--amber)]">
            {{ duration }}
          </div>

          <div class="text-[0.65rem] text-[var(--muted)]">
            SEC
          </div>
        </div>
      </div>

      <pre
        v-if="!analyzing && analysis"
        class="terminal-log custom-scrollbar mt-4 max-h-[52vh] overflow-y-auto whitespace-pre-wrap border border-[var(--border)] p-4 text-[0.72rem] leading-relaxed text-[var(--text)]"
      >{{ analysis }}</pre>

      <div
        v-if="!analyzing && !analysis"
        class="mt-4 border border-[var(--border)] p-6 text-center text-[var(--muted)]"
      >
        분석 결과가 없습니다.
      </div>
    </div>
  </div>
</template>

<style scoped>
.scale-hourglass {
  width: 42px;
  height: 42px;
  font-size: 34px;
  line-height: 42px;
  color: var(--accent);
  animation: hourglassSpin 1.2s infinite steps(2, end);
  text-shadow: 0 0 12px rgba(0, 184, 212, 0.65);
}

@keyframes hourglassSpin {
  0% {
    transform: rotate(0deg);
    opacity: 0.55;
  }

  50% {
    transform: rotate(180deg);
    opacity: 1;
  }

  100% {
    transform: rotate(360deg);
    opacity: 0.55;
  }
}
</style>