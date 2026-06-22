<template>
  <div class="space-y-2">

    <!-- Recommendation Item -->
    <article
      v-for="item in items"
      :key="item.title"
      class="
        queue-item
        p-3
        border-l-2
        border-[#FACC15]
      "
    >


      <!-- Header -->
      <div
        class="
          flex
          items-center
          justify-between
        "
      >

        <div class="flex items-center justify-between">

          <div>
            <strong
              class="
                text-[0.75rem]
                tracking-[0.12em]
                text-[#FACC15]
              "
            >
              {{ item.title }}
            </strong>

            <div
              v-if="item.targetKey"
              class="
                mt-1
                text-[0.6rem]
                tracking-[0.12em]
                text-[var(--muted)]
              "
            >
              TARGET {{ item.targetKey }}
            </div>
          </div>

        </div>


      </div>

      <!-- Message -->
      <p
        class="
          mt-2
          text-[0.75rem]
          leading-relaxed
          text-slate-300
        "
      >
        {{ item.message }}
      </p>

    </article>


    <!-- Empty -->
    <div
      v-if="!items || items.length === 0"
      class="
        py-5
        text-center
        text-xs
        tracking-[0.12em]
        text-[var(--dim)]
      "
    >
      NO AI RECOMMENDATION
    </div>



  </div>
</template>


<script setup>
const emit = defineEmits([
  'open-detail'
])

defineProps({
  items: {
    type: Array,
    default: () => []
  }
})
</script>