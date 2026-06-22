<template>
  <div class="space-y-2">

    <article
      v-for="item in items"
      :key="item.taskId"
      class="queue-item flex items-center justify-between p-2"
    >
      <div class="flex items-center gap-3">

        <!-- 순서 -->
        <span class="text-[0.72rem] text-[var(--muted)]">
          {{ String(item.sequenceOrder ?? item.taskId).padStart(2, '0') }}
        </span>


        <!-- 제품명 -->
        <div>
          <p class="text-[0.78rem] tracking-[0.08em] text-[var(--text)]">
            {{ formatProduct(item.productType) }}
          </p>

          <p class="text-[0.62rem] text-[var(--muted)]">
            QTY {{ item.quantity }}
          </p>
        </div>

      </div>


      <!-- 상태 -->
      <div class="flex items-center gap-2">

        <span
          class="
            border
            px-2
            py-0.5
            text-[0.62rem]
            tracking-[0.12em]
          "
          :class="statusClass(item.status)"
        >
          {{ item.status }}
        </span>

        <button
          v-if="item.status !== 'DONE'"
          class="
            border border-red-500
            px-2
            py-0.5
            text-[0.58rem]
            tracking-[0.12em]
            text-red-400
            hover:bg-red-500
            hover:text-black
          "
          @click.stop="cancelTask(item.taskId)"
        >
          X
        </button>

      </div>

    </article>


    <div
      v-if="items.length === 0"
      class="py-5 text-center text-xs tracking-[0.12em] text-[var(--dim)]"
    >
      NO TASKS
    </div>

  </div>
</template>


<script setup>
import { taskApi } from '../../api/taskApi'

const emit = defineEmits(['cancelled'])

defineProps({
  items: {
    type: Array,
    default: () => [],
  },
})


function formatProduct(type) {
  const map = {
    CAR_CONTROL_UNIT: '차량 제어 장치',
    CAMERA_MODULE: '카메라 센서 모듈',
    BATTERY_PACK: '배터리 팩',
  }

  return map[type] ?? type
}

async function cancelTask(taskId) {
  try {
    await taskApi.cancel(taskId)
    emit('cancelled')
  } catch (e) {
    console.error('[TASK CANCEL FAILED]', e)
  }
}

function statusClass(status) {

  if (status === 'DONE') {
    return 'border-[var(--green)] text-[var(--green)]'
  }

  if (status === 'RUNNING') {
    return 'border-[var(--accent)] text-[var(--accent)]'
  }

  if (status === 'CANCELLED') {
    return 'border-[var(--red)] text-[var(--red)]'
  }

  return 'border-[var(--amber)] text-[var(--amber)]'
}
</script>