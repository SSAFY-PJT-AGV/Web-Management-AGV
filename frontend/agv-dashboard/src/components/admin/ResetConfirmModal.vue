<script setup>
import { ref } from 'vue'
import { adminApi } from '../../api/adminApi'
import { disconnectAllFakeAgvs } from '../../websocket/fakeAgvSocket'

const emit = defineEmits(['close', 'refresh'])

const adminName = ref('')
const reason = ref('')
const confirmText = ref('')
const loading = ref(false)

const errorMessage = ref('')
const successMessage = ref('')

async function reset() {
  errorMessage.value = ''
  successMessage.value = ''

  if (!adminName.value.trim()) {
    errorMessage.value = '관리자 이름을 입력하세요.'
    return
  }

  if (!reason.value.trim()) {
    errorMessage.value = '초기화 사유를 입력하세요.'
    return
  }

  if (confirmText.value !== 'RESET') {
    errorMessage.value = 'RESET을 정확히 입력해야 합니다.'
    return
  }

  try {
    loading.value = true

    disconnectAllFakeAgvs()

    await adminApi.resetDemo({
      adminName: adminName.value.trim(),
      reason: reason.value.trim()
    })

    successMessage.value = '데모 상태가 초기화되었습니다.'

    emit('refresh')
    emit('close')
  } catch (error) {
    console.error('[RESET DEMO ERROR]', error)

    errorMessage.value =
      error?.response?.data?.error?.message ||
      error?.response?.data?.message ||
      '초기화에 실패했습니다. 서버 로그를 확인하세요.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/70">
    <div class="w-96 border border-[var(--border)] bg-black p-6">
      <h2 class="mb-3 text-sm tracking-[0.18em] text-red-400">
        RESET DEMO STATE
      </h2>

      <p class="mb-4 text-xs leading-5 text-[var(--muted)]">
        Mission, Reservation, Task 상태가 초기화되고<br>
        AGV는 OFFLINE이 됩니다.<br>
        실행 전 관리자 이름과 사유를 남겨주세요.
      </p>

      <input
        v-model="adminName"
        placeholder="Admin Name"
        class="mb-2 w-full border border-[var(--border)] bg-black p-2 text-white"
      />

      <input
        v-model="reason"
        placeholder="Reason"
        class="mb-2 w-full border border-[var(--border)] bg-black p-2 text-white"
      />

      <input
        v-model="confirmText"
        placeholder="Type RESET"
        class="mb-3 w-full border border-red-500 bg-black p-2 text-white"
      />

      <p
        v-if="errorMessage"
        class="mb-3 text-xs text-red-400"
      >
        {{ errorMessage }}
      </p>

      <p
        v-if="successMessage"
        class="mb-3 text-xs text-green-400"
      >n
        {{ successMessage }}
      </p>

      <div class="flex justify-end gap-2">
        <button
          class="border border-[var(--border)] px-3 py-1 text-xs text-[var(--muted)]"
          :disabled="loading"
          @click="$emit('close')"
        >
          CANCEL
        </button>

        <button
          class="border border-red-500 px-3 py-1 text-xs text-red-400 disabled:opacity-40"
          :disabled="loading"
          @click="reset"
        >
          {{ loading ? 'RESETTING...' : 'CONFIRM RESET' }}
        </button>
      </div>
    </div>
  </div>
</template>