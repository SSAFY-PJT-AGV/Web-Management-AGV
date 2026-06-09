<template>
  <div class="space-y-2">

    <article
      v-for="item in displayItems"
      :key="item.order"
      class="queue-item flex items-center justify-between p-2"
    >
      <div class="flex items-center gap-3">
        <span class="text-[0.72rem] text-[var(--muted)]">
          {{ String(item.order).padStart(2, '0') }}
        </span>

        <span class="text-[0.78rem] tracking-[0.08em] text-[var(--text)]">
          {{ item.productName }}
        </span>
      </div>

      <span class="border border-[var(--green)] px-2 py-0.5 text-[0.62rem] tracking-[0.12em] text-[var(--green)]">
        READY
      </span>
    </article>

    <div
      v-if="displayItems.length === 0"
      class="py-5 text-center text-xs tracking-[0.12em] text-[var(--dim)]"
    >
      NO OUTBOUND ITEMS
    </div>

  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  items: {
    type: Array,
    default: () => []
  }
})

const mockItems = [
  {
    order: 1,
    productName: '차량 제어 장치'
  },
  {
    order: 2,
    productName: '카메라 센서 모듈'
  }
]

const displayItems = computed(() => {
  return props.items.length > 0
    ? props.items
    : mockItems
})
</script>