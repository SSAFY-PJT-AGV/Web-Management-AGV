<template>
  <main class="hmi-root grid-background p-2">
    <div class="relative z-50 flex items-center gap-2 overflow-visible">
      <div class="flex-1">
        <HeaderStatus :connected="connected" />
      </div>

      <AdminMenu
        :agvs="agv.items"
        @refresh="refreshDashboard"
      />
    </div>

    <section class="hmi-layout">
      <aside class="flex flex-col gap-3 min-h-0">
        <PanelFrame title="AGV CURRENT STATE" class="shrink-0">
          <div class="grid grid-cols-1 gap-2">
            <AgvStatusCard
              v-for="a in displayAgvs"
              :key="a.agvId"
              :agv="a"
              :missions="mission.items"
              :now="now"
            />
          </div>
        </PanelFrame>

        <PanelFrame title="AGV CONTROL" class="shrink-0">
          <AgvControl :agvs="displayAgvs" />
        </PanelFrame>

        <PanelFrame title="EVENT LOG" class="flex-1 min-h-0 overflow-hidden">
          <div class="h-full min-h-0 overflow-y-auto pr-1">
            <EventLog :items="event.items" />
          </div>
        </PanelFrame>
      </aside>

      <section class="flex flex-col gap-3 min-h-0">
        <PanelFrame title="FACTORY OPERATION SUMMARY" class="shrink-0">
          <DispatchStatus
            :agvs="displayAgvs"
            :missions="mission.items"
            :connected="connected"
            :tasks="task.items"
            :now="now"
          />
        </PanelFrame>

        <PanelFrame title="FACTORY DIGITAL MAP" class="flex-[1.7] min-h-0 overflow-hidden">
          <div class="h-full min-h-0 overflow-hidden">
            <FactoryMap
              :markers="map.markers"
              :agvs="displayMapAgvs"
              :marker-by-id="map.markerById"
            />
          </div>
        </PanelFrame>

        <PanelFrame title="MISSION FLOW" class="flex-[0.9] min-h-0 overflow-hidden">
          <div class="grid h-full min-h-0 grid-cols-2 gap-3">
            <PanelFrame title="AGV01 CURRENT & NEXT" class="min-h-0 overflow-hidden">
              <div class="h-full min-h-0 overflow-hidden">
                <MissionQueue
                  :items="agv01Missions"
                  :agv-status="agv01Status"
                />
              </div>
            </PanelFrame>

            <PanelFrame title="AGV02 CURRENT & NEXT" class="min-h-0 overflow-hidden">
              <div class="h-full min-h-0 overflow-hidden">
                <MissionQueue
                  :items="agv02Missions"
                  :agv-status="agv02Status"
                />
              </div>
            </PanelFrame>
          </div>
        </PanelFrame>
      </section>

      <aside class="flex flex-col gap-2 min-h-0 overflow-hidden">
        <PanelFrame title="OPERATOR REQUEST" class="shrink-0">
          <CommandPanel />
        </PanelFrame>

        <PanelFrame title="INVENTORY" class="h-[clamp(140px,18vh,200px)] shrink-0 overflow-hidden relative">
          <InventoryPanel :items="inventory.items" />
        </PanelFrame>

        <PanelFrame
          title="AI OPERATOR BRIEF"
          class="h-[300px] shrink-0 overflow-hidden relative"
        >
          <template #action>
            <button
              class="
                border border-[var(--accent)]
                px-3 py-1
                text-[0.6rem]
                tracking-[0.18em]
                text-[var(--accent)]
                hover:bg-[var(--accent)]
                hover:text-black
              "
              @click="showAiDetail = true"
            >
              MORE
            </button>
          </template>

          <div class="absolute inset-x-4 top-[54px] bottom-3 overflow-y-auto pr-2">
            <AiRecommendation
              :items="rec.items"
              :agvs="displayAgvs"
              :missions="mission.items"
              :inventories="inventory.items"
              :now="now"
            />
          </div>
        </PanelFrame>

        <PanelFrame title="OUTBOUND QUEUE" class="flex-1 min-h-0 overflow-hidden">
          <div class="h-full min-h-0 overflow-y-auto pr-1">
            <OutboundQueue
              :items="activeTasks"
              @cancelled="refreshDashboard"
            />
          </div>
        </PanelFrame>
      </aside>
    </section>

    <AiDetailPanel
      v-if="showAiDetail"
      :agvs="displayAgvs"
      :missions="mission.items"
      :inventories="inventory.items"
      :recommendations="rec.items"
      :now="now"
      @close="showAiDetail = false"
    />
  </main>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'

import HeaderStatus from '../components/layout/HeaderStatus.vue'
import AdminMenu from '../components/admin/AdminMenu.vue'
import PanelFrame from '../components/layout/PanelFrame.vue'
import AgvStatusCard from '../components/agv/AgvStatusCard.vue'
import AgvControl from '../components/agv/AgvControl.vue'
import MissionQueue from '../components/mission/MissionQueue.vue'
import DispatchStatus from '../components/dispatch/DispatchStatus.vue'
import FactoryMap from '../components/map/FactoryMap.vue'
import CommandPanel from '../components/command/CommandPanel.vue'
import InventoryPanel from '../components/inventory/InventoryPanel.vue'
import AiRecommendation from '../components/ai/AiRecommendation.vue'
import AiDetailPanel from '../components/ai/AiDetailPanel.vue'
import EventLog from '../components/event/EventLog.vue'
import OutboundQueue from '../components/outbound/OutboundQueue.vue'

import { useAgvStore } from '../stores/agvStore'
import { useMissionStore } from '../stores/missionStore'
import { useInventoryStore } from '../stores/inventoryStore'
import { useRecommendationStore } from '../stores/recommendationStore'
import { useEventStore } from '../stores/eventStore'
import { useMapStore } from '../stores/mapStore'
import { useTaskStore } from '../stores/taskStore'

import { connectDashboardSocket } from '../websocket/dashboardSocket'
import { connectFakeAgv } from '../websocket/fakeAgvSocket'

const connected = ref(false)
const showAiDetail = ref(false)
const now = ref(Date.now())

let dashboardWs = null
let clockTimer = null

const agv = useAgvStore()
const mission = useMissionStore()
const inventory = useInventoryStore()
const rec = useRecommendationStore()
const event = useEventStore()
const map = useMapStore()
const task = useTaskStore()

const activeMissionStatuses = [
  'ASSIGNED',
  'CREATED',
  'IN_PROGRESS',
  'FAILED'
]

const displayAgvs = computed(() =>
  agv.items.filter(a =>
    String(a.agvId) === '1' ||
    String(a.agvId) === '2'
  )
)

const displayMapAgvs = computed(() =>
  map.agvs.filter(a =>
    String(a.agvId) === '1' ||
    String(a.agvId) === '2'
  )
)

const agv01Missions = computed(() =>
  mission.items.filter(m =>
    String(m.agvId) === '1' &&
    activeMissionStatuses.includes(m.status)
  )
)

const agv02Missions = computed(() =>
  mission.items.filter(m =>
    String(m.agvId) === '2' &&
    activeMissionStatuses.includes(m.status)
  )
)

const agv01Status = computed(() =>
  agv.items.find(a => String(a.agvId) === '1')?.status ?? 'OFFLINE'
)

const agv02Status = computed(() =>
  agv.items.find(a => String(a.agvId) === '2')?.status ?? 'OFFLINE'
)

const activeTaskStatuses = [
  'READY',
  'RUNNING',
  'IN_PROGRESS'
]

const activeTasks = computed(() =>
  task.items.filter(t =>
    activeTaskStatuses.includes(t.status)
  )
)

async function refreshDashboard() {
  await Promise.allSettled([
    agv.load(),
    mission.load(),
    inventory.load(),
    map.load(),
    event.load(),
    rec.load(),
    task.load()
  ])

  map.setAgvs(displayAgvs.value)
}

onMounted(async () => {
  clockTimer = setInterval(() => {
    now.value = Date.now()
  }, 1000)

  await refreshDashboard()

  if (localStorage.getItem('fakeAgv1') === 'true') {
    connectFakeAgv(1)
  }

  if (localStorage.getItem('fakeAgv2') === 'true') {
    connectFakeAgv(2)
  }

  dashboardWs = connectDashboardSocket({
    onOpen: () => {
      connected.value = true
    },

    onClose: () => {
      connected.value = false
    },

    onError: () => {
      connected.value = false
    },

    onMessage: msg => {
      console.log('[DASHBOARD WS MESSAGE]', msg)

      switch (msg.type) {
        case 'AGV_STATUS':
        case 'AGV_STATUS_UPDATED':
          agv.update(msg.data)

          if (!msg.data?.testMode) {
            map.updateAgv(msg.data)
          }

          mission.load().catch(console.error)
          break

        case 'TASK_REFRESH':
          task.load().catch(console.error)
          break

        case 'MISSION_REFRESH':
          Promise.allSettled([
            mission.load(),
            agv.load(),
            map.load()
          ])
            .then(() => {
              map.setAgvs(displayAgvs.value)
            })
            .catch(console.error)
          break

        case 'INVENTORY_REFRESH':
          inventory.load().catch(console.error)
          break

        case 'EVENT_REFRESH':
          event.load().catch(console.error)
          break

        case 'AI_REFRESH':
          rec.load().catch(console.error)
          break

        case 'MAP_REFRESH':
          map.load().catch(console.error)
          break

        default:
          console.warn('[UNKNOWN DASHBOARD WS MESSAGE]', msg)
      }
    },
  })
})

onUnmounted(() => {
  clearInterval(clockTimer)
  dashboardWs?.close()
})
</script>