<template>
  <div class="factory-map h-full min-h-[390px] overflow-hidden border border-[var(--border)]">
    <svg viewBox="0 0 900 850" class="h-full w-full">

      <text x="455" y="42" text-anchor="middle" fill="var(--accent)" font-size="17" font-weight="900" letter-spacing="3">
        FACTORY MAP
      </text>

      <g :transform="`translate(${mapGroupX} ${mapGroupY}) scale(${mapScale})`">

          <path
            d="
              M 180 160
              H 675
              Q 805 160 805 290
              V 700
              Q 805 775 730 775
              H 180
              Q 105 775 105 700
              V 300
              Q 105 160 180 160
            "
            fill="none"
            stroke="#111827"
            stroke-width="30"
            stroke-linecap="round"
            stroke-linejoin="round"
          />

          <path
            d="M 805 90 V 705"
            fill="none"
            stroke="#111827"
            stroke-width="24"
            stroke-linecap="round"
          />

          <!-- Center Storage Group -->
          <g :transform="`translate(${storageGroupX} ${storageGroupY})`">

            <!-- Finished Product Storage -->
            <text
              x="0"
              y="0"
              text-anchor="middle"
              fill="#F8FAFC"
              font-size="20"
              font-weight="900"
            >
              완제품 상자 보관 구역
            </text>

            <g transform="translate(-120 55)">
              <rect x="-24" y="-24" width="48" height="48" fill="var(--accent)" />
              <text y="58" text-anchor="middle" fill="#E2E8F0" font-size="16" font-weight="1100">차량</text>
              <text y="78" text-anchor="middle" fill="#E2E8F0" font-size="16" font-weight="1100">제어 장치</text>
            </g>

            <g transform="translate(0 55)">
              <rect x="-24" y="-24" width="48" height="48" fill="var(--accent)"  />
              <text y="58" text-anchor="middle" fill="#E2E8F0" font-size="16" font-weight="1100">카메라</text>
              <text y="78" text-anchor="middle" fill="#E2E8F0" font-size="16" font-weight="1100">센서 모듈</text>
            </g>

            <g transform="translate(120 55)">
              <rect x="-24" y="-24" width="48" height="48" fill="var(--accent)"  />
              <text y="58" text-anchor="middle" fill="#E2E8F0" font-size="16" font-weight="1100">배터리</text>
              <text y="78" text-anchor="middle" fill="#E2E8F0" font-size="16" font-weight="1100">팩</text>
            </g>


            <!-- Material Storage -->
            <text
              x="0"
              y="280"
              text-anchor="middle"
              fill="#F8FAFC"
              font-size="20"
              font-weight="900"
            >
              자재 상자 보관 구역
            </text>

            <g transform="translate(-120 335)">
              <rect x="-24" y="-24" width="48" height="48" fill="var(--green)"  />
              <text y="58" text-anchor="middle" fill="#E2E8F0" font-size="17" font-weight="900">CHIP</text>
              <text y="78" text-anchor="middle" fill="#94A3B8" font-size="15" font-weight="700">칩</text>
            </g>

            <g transform="translate(0 335)">
              <rect x="-24" y="-24" width="48" height="48" fill="var(--green)"  />
              <text y="58" text-anchor="middle" fill="#E2E8F0" font-size="17" font-weight="900">SENSOR</text>
              <text y="78" text-anchor="middle" fill="#94A3B8" font-size="15" font-weight="700">센서</text>
            </g>

            <g transform="translate(120 335)">
              <rect x="-24" y="-24" width="48" height="48" fill="var(--green)"  />
              <text y="58" text-anchor="middle" fill="#E2E8F0" font-size="17" font-weight="900">BATTERY</text>
              <text y="78" text-anchor="middle" fill="#94A3B8" font-size="15" font-weight="700">배터리</text>
            </g>

          </g>

          <!-- 기존 주변 구역 유지 -->
          <rect x="60" y="310" width="90" height="220" fill="rgba(34,197,94,0.26)" stroke="#22C55E" stroke-width="2" />
          <text x="105" y="300" text-anchor="middle" fill="#A7F3D0" font-size="14" font-weight="900">컨베이어</text>

          <rect x="735" y="430" width="140" height="105" fill="rgba(250,204,21,0.16)" stroke="#FACC15" stroke-width="2" />
          <text x="805" y="488" text-anchor="middle" fill="#FACC15" font-size="16" font-weight="900">교차 구역</text>

          <rect x="720" y="65" width="170" height="72" fill="rgba(45,212,191,0.24)" stroke="#2DD4BF" stroke-width="2" />
          <text x="805" y="108" text-anchor="middle" fill="#CCFBF1" font-size="16" font-weight="900">입출고 구역</text>

          <ZoneMarker
            v-for="marker in markers"
            :key="marker.markerId"
            :marker="marker"
          />

          <AgvMarker
            v-for="agv in agvs"
            :key="agv.agvId"
            :agv="agv"
            :x="markerById(agv.currentMarker)?.x || 30"
            :y="markerById(agv.currentMarker)?.y || 30"
          />
        </g>
    </svg>
  </div>
</template>

<script setup>
import { computed } from 'vue'

import ZoneMarker from './ZoneMarker.vue'
import AgvMarker from './AgvMarker.vue'

defineProps({
  markers: {
    type: Array,
    default: () => []
  },
  agvs: {
    type: Array,
    default: () => []
  },
  markerById: {
    type: Function,
    required: true
  }
})

const VIEWBOX_WIDTH = 900
const VIEWBOX_HEIGHT = 850

const mapScale = 0.95

const mapGroupX = computed(() => VIEWBOX_WIDTH * 0.017)
const mapGroupY = computed(() => VIEWBOX_HEIGHT * 0.024)

// 전체 트랙 내부 기준 중앙
const storageGroupX = computed(() => VIEWBOX_WIDTH * 0.51)
const storageGroupY = computed(() => VIEWBOX_HEIGHT * 0.305)
</script>