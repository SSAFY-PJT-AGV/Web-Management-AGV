<template>
  <div class="space-y-2">

    <article
      v-for="item in normalizedItems"
      :key="item.material"
      class="queue-item p-3"
    >
      <div class="flex items-center justify-between">

        <div>
          <div class="font-['Barlow_Condensed'] text-[1.15rem] font-bold tracking-[0.14em] text-[var(--text)]">
            {{ item.material }}
          </div>

          <div class="text-[0.62rem] tracking-[0.12em] text-[var(--muted)]">
            {{ item.label }}
          </div>
        </div>

        <div class="flex items-center gap-3">
          <span
            class="text-[1.25rem] font-black"
            :class="quantityClass(item.status)"
          >
            {{ item.currentQuantity }}
          </span>

          <span
            class="border px-2 py-0.5 text-[0.62rem] tracking-[0.12em]"
            :class="statusBadgeClass(item.status)"
          >
            {{ item.status }}
          </span>
        </div>

      </div>

      <div class="mt-3 h-1.5 bg-[#111827]">
        <div
          class="h-full transition-all duration-300"
          :class="barClass(item.status)"
          :style="{ width: getPercentage(item) + '%' }"
        ></div>
      </div>
    </article>

    <div
      v-if="normalizedItems.length === 0"
      class="py-5 text-center text-xs tracking-[0.12em] text-[var(--dim)]"
    >
      NO INVENTORY DATA
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

const materialLabelMap = {
  CHIP: '칩',
  SENSOR: '센서',
  BATTERY: '배터리 부품'
}

const normalizedItems = computed(() => {
  return props.items.slice(0, 4).map(item => {
    const material =
      item.material ||
      item.materialCode ||
      item.name ||
      'UNKNOWN'

    return {
      material,
      label:
        item.label ||
        item.materialName ||
        materialLabelMap[material] ||
        material,

      currentQuantity:
        item.currentQuantity ??
        item.quantity ??
        0,

      minThreshold:
        item.minThreshold ??
        item.minimumQuantity ??
        1,

      status:
        item.status ||
        'NORMAL'
    }
  })
})

function getPercentage(item) {
  const max = item.minThreshold * 3

  if (!max) {
    return 0
  }

  return Math.min(
    100,
    (item.currentQuantity / max) * 100
  )
}

function quantityClass(status) {
  const classMap = {
    NORMAL: 'text-[var(--green)]',
    LOW: 'text-[var(--amber)]',
    SHORTAGE: 'text-[var(--red)]'
  }

  return classMap[status] || 'text-[var(--text)]'
}

function statusBadgeClass(status) {
  const classMap = {
    NORMAL: 'border-[var(--green)] bg-[rgba(0,230,118,0.06)] text-[var(--green)]',
    LOW: 'border-[var(--amber)] bg-[rgba(255,171,0,0.06)] text-[var(--amber)]',
    SHORTAGE: 'border-[var(--red)] bg-[rgba(255,23,68,0.06)] text-[var(--red)]'
  }

  return classMap[status] || 'border-[var(--border2)] text-[var(--muted)]'
}

function barClass(status) {
  const classMap = {
    NORMAL: 'bg-[var(--green)]',
    LOW: 'bg-[var(--amber)]',
    SHORTAGE: 'bg-[var(--red)]'
  }

  return classMap[status] || 'bg-[var(--dim)]'
}
</script>