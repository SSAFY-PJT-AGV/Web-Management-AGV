<template>
  <section class="panel-frame rounded-sm p-3 min-h-0">

    <!-- Panel Header -->
    <header
      v-if="title"
      class="
        mb-2
        flex
        items-center
        justify-between
        border-b
        border-[#1E3A4F]
        pb-2
      "
    >
      <h2
        class="
          panel-title
          text-sm
          font-black
          uppercase
          tracking-[0.18em]
          text-[#38BDF8]
        "
      >
        {{ title }}
      </h2>

      <!-- Header Action Slot -->
      <slot name="action" />

    </header>


    <!-- Panel Content -->
    <div class="min-h-0">
      <slot />
    </div>

  </section>
</template>


<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>