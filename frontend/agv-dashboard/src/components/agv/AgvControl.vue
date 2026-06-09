<template>
  <div class="grid grid-cols-2 gap-2">

    <button
      v-for="agv in agvs"
      :key="agv.agvId"
      class="queue-item p-2 text-left"
    >
      <b>
        {{ agv.agvId }}
      </b>


      <div class="mt-2 grid grid-cols-3 gap-1 text-xs">

        <button
          class="
            bg-[#101826]
            border
            border-[#FACC15]
            p-1
          "
          @click="pause(agv.agvId)"
        >
          정지
        </button>


        <button
          class="
            bg-[#101826]
            border
            border-[#22C55E]
            p-1
          "
          @click="resume(agv.agvId)"
        >
          재개
        </button>


        <button
          class="
            bg-[#101826]
            border
            border-[#EF4444]
            p-1
          "
          @click="cancel(agv.agvId)"
        >
          취소
        </button>

      </div>

    </button>

  </div>
</template>


<script setup>
import { agvApi } from '../../api/agvApi'


defineProps({
  agvs: {
    type: Array,
    default: () => []
  }
})


const pause = id =>
  agvApi.pause(id)


const resume = id =>
  agvApi.resume(id)


const cancel = id =>
  agvApi.cancel(id)
</script>