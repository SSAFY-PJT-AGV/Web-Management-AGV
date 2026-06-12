<script setup>
import { ref } from 'vue'
import ResetConfirmModal from './ResetConfirmModal.vue'
import { connectFakeAgv } from '../../websocket/fakeAgvSocket'

const open = ref(false)
const showReset = ref(false)

const emit = defineEmits(['refresh'])

const props = defineProps({
  agvs: {
    type: Array,
    default: () => []
  }
})

function isOffline(id) {
  const agv = props.agvs.find(a => String(a.agvId) === String(id))
  return !agv || agv.status === 'OFFLINE'
}

function connectAgv(id) {
  connectFakeAgv(id)

  setTimeout(() => {
    emit('refresh')
  }, 500)
}
</script>

<template>
  <div class="relative z-[9999]">
    <button
      class="border px-3 py-1 text-xs"
      @click="open = !open"
    >
      ADMIN
    </button>

    <div
      v-if="open"
      class="absolute right-0 top-full z-[9999] mt-2 w-52 border border-[var(--border)] bg-[var(--panel)] p-2 shadow-lg"
    >
      <button
        :disabled="!isOffline(1)"
        :class="{ 'opacity-40 cursor-not-allowed': !isOffline(1) }"
        @click="connectAgv(1)"
      >
        ADD AGV01
      </button>

      |

      <button
        :disabled="!isOffline(2)"
        :class="{ 'opacity-40 cursor-not-allowed': !isOffline(2) }"
        @click="connectAgv(2)"
      >
        ADD AGV02
      </button>

      <button
        class="text-red-400"
        @click="showReset = true"
      >
        RESET DEMO
      </button>
    </div>

    <ResetConfirmModal
      v-if="showReset"
      @close="showReset = false"
      @refresh="emit('refresh')"
    />
  </div>
</template>