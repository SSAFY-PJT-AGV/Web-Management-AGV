<script setup>
import { computed, ref } from 'vue'
import ScaleAnalysisPanel from './ScaleAnalysisPanel.vue'

import ResetConfirmModal from './ResetConfirmModal.vue'
import { connectFakeAgv } from '../../websocket/fakeAgvSocket'
import { adminApi } from '../../api/adminApi'

const open = ref(false)
const demoOpen = ref(false)
const scaleOpen = ref(false)
const showReset = ref(false)

const analyzing = ref(false)
const preparing = ref(false)
const cleaning = ref(false)
const analysis = ref('')
const showScaleAnalysis = ref(false)

const emit = defineEmits(['refresh'])

const props = defineProps({
  agvs: {
    type: Array,
    default: () => []
  }
})

const scale = ref({
  supplyCount: 1,
  collectCount: 1,
  fps: 10,
  duration: 60
})

const totalScaleAgv = computed(() =>
  Number(scale.value.supplyCount || 0) +
  Number(scale.value.collectCount || 0)
)

function isOffline(id) {
  const agv = props.agvs.find(a =>
    String(a.agvId) === String(id)
  )

  return !agv || agv.status === 'OFFLINE'
}

function connectAgv(id) {
  connectFakeAgv(id)

  setTimeout(() => {
    emit('refresh')
  }, 500)
}

function validateScale() {
  if (totalScaleAgv.value <= 0 || totalScaleAgv.value > 20) {
    alert('테스트 AGV는 1대 이상, 최대 20대까지 가능합니다.')
    return false
  }

  if (scale.value.fps <= 0 || scale.value.fps > 30) {
    alert('FPS는 1~30 사이로 입력하세요.')
    return false
  }

  if (scale.value.duration <= 0 || scale.value.duration > 600) {
    alert('Duration은 1~600초 사이로 입력하세요.')
    return false
  }

  return true
}

async function prepareScaleTest() {
  if (!validateScale()) return

  preparing.value = true

  try {
    await adminApi.prepareScaleTest({
      supplyCount: scale.value.supplyCount,
      collectCount: scale.value.collectCount
    })

    alert('Scale Test AGV 준비 완료')
    emit('refresh')
  } finally {
    preparing.value = false
  }
}

async function cleanupScaleTest() {
  cleaning.value = true

  try {
    await adminApi.cleanupScaleTest()

    alert('테스트 AGV 정리 완료')
    emit('refresh')
  } finally {
    cleaning.value = false
  }
}

async function analyzeScale() {
  if (!validateScale()) return

  analyzing.value = true
  analysis.value = ''
  showScaleAnalysis.value = true

  try {
    const res = await adminApi.analyzeScaleTest(
      totalScaleAgv.value,
      scale.value.fps
    )

    analysis.value = res.data
  } finally {
    analyzing.value = false
  }
}
</script>

<template>
  <div class="relative z-[9999] shrink-0">
    <button
      class="control-button px-3 py-1"
      @click="open = !open"
    >
      ADMIN
    </button>

    <div
      v-if="open"
      class="absolute right-0 top-full z-[9999] mt-2 w-[420px]"
    >
      <div class="panel-frame p-3">
        <div class="mb-3 flex items-center justify-between">
          <div class="panel-title">
            ADMIN CONTROL
          </div>

          <button
            class="control-button px-2 py-1 text-[0.65rem]"
            @click="open = false"
          >
            CLOSE
          </button>
        </div>

        <div class="space-y-2">
          <button
            class="control-button flex w-full items-center justify-between px-3 py-2"
            @click="demoOpen = !demoOpen"
          >
            <span>DEMO CONTROL</span>
            <span>{{ demoOpen ? 'OPEN' : 'FOLDED' }}</span>
          </button>

          <div
            v-if="demoOpen"
            class="queue-item p-3"
          >
            <div class="flex gap-2">
              <button
                class="control-button px-3 py-1"
                :disabled="!isOffline(1)"
                :class="{ 'opacity-40 cursor-not-allowed': !isOffline(1) }"
                @click="connectAgv(1)"
              >
                CONNECT AGV01
              </button>

              <button
                class="control-button px-3 py-1"
                :disabled="!isOffline(2)"
                :class="{ 'opacity-40 cursor-not-allowed': !isOffline(2) }"
                @click="connectAgv(2)"
              >
                CONNECT AGV02
              </button>
            </div>

            <button
              class="control-button mt-2 px-3 py-1 text-[var(--red)]"
              @click="showReset = true"
            >
              RESET DEMO
            </button>
          </div>

          <button
            class="control-button flex w-full items-center justify-between px-3 py-2"
            @click="scaleOpen = !scaleOpen"
          >
            <span>SCALE TEST</span>
            <span>{{ scaleOpen ? 'OPEN' : 'FOLDED' }}</span>
          </button>

          <div
            v-if="scaleOpen"
            class="queue-item p-3"
          >
            <div class="grid grid-cols-2 gap-2">
              <label class="text-[0.68rem] tracking-[0.12em] text-[var(--muted)]">
                SUPPLY
                <input
                  v-model.number="scale.supplyCount"
                  type="number"
                  min="0"
                  max="20"
                  class="mt-1 w-full border border-[var(--border2)] bg-[var(--bg)] px-2 py-1 text-[var(--text)]"
                />
              </label>

              <label class="text-[0.68rem] tracking-[0.12em] text-[var(--muted)]">
                COLLECT
                <input
                  v-model.number="scale.collectCount"
                  type="number"
                  min="0"
                  max="20"
                  class="mt-1 w-full border border-[var(--border2)] bg-[var(--bg)] px-2 py-1 text-[var(--text)]"
                />
              </label>

              <label class="text-[0.68rem] tracking-[0.12em] text-[var(--muted)]">
                FPS
                <input
                  v-model.number="scale.fps"
                  type="number"
                  min="1"
                  max="30"
                  class="mt-1 w-full border border-[var(--border2)] bg-[var(--bg)] px-2 py-1 text-[var(--text)]"
                />
              </label>

              <label class="text-[0.68rem] tracking-[0.12em] text-[var(--muted)]">
                DURATION
                <input
                  v-model.number="scale.duration"
                  type="number"
                  min="1"
                  max="600"
                  class="mt-1 w-full border border-[var(--border2)] bg-[var(--bg)] px-2 py-1 text-[var(--text)]"
                />
              </label>
            </div>

            <div class="mt-2 text-[0.68rem] tracking-[0.12em] text-[var(--muted)]">
              TARGET:
              {{ totalScaleAgv }} AGV × {{ scale.fps }} FPS =
              <span class="text-[var(--accent)]">
                {{ totalScaleAgv * scale.fps }} MSG/S
              </span>
            </div>

            <div class="mt-3 grid grid-cols-3 gap-2">
              <button
                class="control-button px-2 py-1 text-[var(--accent)]"
                :disabled="preparing"
                @click="prepareScaleTest"
              >
                {{ preparing ? 'READY...' : 'PREPARE' }}
              </button>

              <button
                class="control-button px-2 py-1 text-[var(--green)]"
                :disabled="analyzing"
                @click="analyzeScale"
              >
                {{ analyzing ? 'AI...' : 'AI ANALYZE' }}
              </button>

              <button
                class="control-button px-2 py-1 text-[var(--amber)]"
                :disabled="cleaning"
                @click="cleanupScaleTest"
              >
                {{ cleaning ? 'CLEAN...' : 'CLEANUP' }}
              </button>
            </div>

            <pre
              v-if="analysis"
              class="terminal-log custom-scrollbar mt-3 max-h-52 overflow-y-auto whitespace-pre-wrap border border-[var(--border)] p-2 text-[0.68rem] leading-relaxed text-[var(--text)]"
            >{{ analysis }}</pre>
          </div>
        </div>
      </div>
    </div>

    <ResetConfirmModal
      v-if="showReset"
      @close="showReset = false"
      @refresh="emit('refresh')"
    />

    <ScaleAnalysisPanel
      :open="showScaleAnalysis"
      :analyzing="analyzing"
      :analysis="analysis"
      :agv-count="totalScaleAgv"
      :fps="scale.fps"
      :duration="scale.duration"
      @close="showScaleAnalysis = false"
    />
  </div>
</template>