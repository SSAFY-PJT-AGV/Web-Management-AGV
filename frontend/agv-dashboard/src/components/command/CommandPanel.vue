<template>
  <div class="space-y-3">

    <!-- Product Select -->
    <div>
      <p class="label">PRODUCT TYPE</p>

      <select
        v-model="productType"
        class="input-box"
      >
        <option value="CAR_CONTROL_UNIT">
          차량 제어 장치
        </option>

        <option value="CAMERA_MODULE">
          카메라 센서 모듈
        </option>

        <option value="BATTERY_PACK">
          배터리 팩
        </option>
      </select>
    </div>


    <!-- Quantity -->
    <div>
      <p class="label">QUANTITY</p>

      <div class="grid grid-cols-3 gap-2">

        <button
          class="control-btn"
          @click="decrease"
        >
          -
        </button>


        <div class="quantity-box">
          {{ quantity }}
        </div>


        <button
          class="control-btn"
          @click="increase"
        >
          +
        </button>

      </div>
    </div>


    <!-- Submit -->
    <button
      class="submit-btn"
      @click="requestProduction"
    >
      CREATE PRODUCTION TASK
    </button>


    <p
      v-if="message"
      class="text-[10px] text-cyan-300"
    >
      {{ message }}
    </p>

    <p
      v-if="error"
      class="text-[10px] text-red-400"
    >
      {{ error }}
    </p>

  </div>
</template>


<script setup>
import { ref } from 'vue'
import { taskApi } from '../../api/taskApi'


const productType = ref('CAR_CONTROL_UNIT')
const quantity = ref(1)

const message = ref('')
const error = ref('')

function increase() {
  quantity.value++
}


function decrease() {
  if (quantity.value > 1) {
    quantity.value--
  }
}


async function requestProduction() {
  message.value = ''
  error.value = ''

  try {
    await taskApi.createTask({
      taskType: 'PRODUCTION',
      productType: productType.value,
      quantity: quantity.value,
    })

    message.value = 'TASK CREATED'

  } catch (e) {
    console.error(e)
    error.value = 'PRODUCTION REQUEST FAILED'
  }
}
</script>


<style scoped>
.label {
  color: #64748b;
  font-size: 10px;
  font-weight: 900;
  letter-spacing: .2em;
  margin-bottom: 6px;
}


.input-box {
  width: 100%;
  background: #020617;
  border: 1px solid #0891b2;
  color: #a5f3fc;
  padding: 8px;
}


.control-btn {
  border: 1px solid #22d3ee;
  color: #22d3ee;
  padding: 8px;
}


.quantity-box {
  display:flex;
  align-items:center;
  justify-content:center;

  border:1px solid #334155;
  color:white;
  font-weight:900;
}


.submit-btn {
  width:100%;

  border:1px solid #22c55e;
  color:#22c55e;

  padding:10px;
  font-weight:900;
}
</style>