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
          <PanelFrame title="AGV STATUS" class="shrink-0">
            <div class="space-y-3">
              <AgvStatusCard
                v-for="a in agv.items"
                :key="a.agvId"
                :agv="a"
              />
            </div>
          </PanelFrame>

          <PanelFrame title="AGV CONTROL" class="shrink-0">
            <AgvControl :agvs="agv.items" />
          </PanelFrame>

          <div class="grid grid-cols-2 gap-3 flex-1 min-h-0">
            <PanelFrame title="AGV01 MISSION QUEUE" class="min-h-0 overflow-hidden">
              <div class="h-full min-h-0 overflow-y-auto pr-1">
                <MissionQueue
                  :items="agv01Missions"
                  :agv-status="agv01Status"
                />
              </div>
            </PanelFrame>

            <PanelFrame title="AGV02 MISSION QUEUE" class="min-h-0 overflow-hidden">
              <div class="h-full min-h-0 overflow-y-auto pr-1">
                <MissionQueue
                  :items="agv02Missions"
                  :agv-status="agv02Status"
                />
              </div>
            </PanelFrame>
          </div>
        </aside>

        <section class="flex flex-col gap-3 min-h-0">
          <PanelFrame title="DISPATCH STATUS" class="shrink-0">
            <DispatchStatus
              :agvs="agv.items"
              :missions="mission.items"
              :connected="connected"
            />
          </PanelFrame>

          <PanelFrame title="FACTORY DIGITAL MAP" class="flex-[1.7] min-h-0 overflow-hidden">
            <div class="h-full min-h-0 overflow-hidden">
              <FactoryMap
                :markers="map.markers"
                :agvs="map.agvs"
                :marker-by-id="map.markerById"
              />
            </div>
          </PanelFrame>

          <PanelFrame title="EVENT LOG" class="flex-[0.75] min-h-0 overflow-hidden">
            <div class="h-full min-h-0 overflow-y-auto pr-1">
              <EventLog :items="event.items" />
            </div>
          </PanelFrame>
        </section>

        <aside class="flex flex-col gap-2 min-h-0 overflow-hidden">
          <PanelFrame title="OPERATOR REQUEST" class="shrink-0">
            <CommandPanel />
          </PanelFrame>

          <PanelFrame title="INVENTORY" class="h-[200px] shrink-0 overflow-hidden relative">
            <InventoryPanel :items="inventory.items" />
          </PanelFrame>

          <PanelFrame
            title="AI RECOMMENDATION"
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
              <AiRecommendation :items="rec.items" />
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
              :agvs="agv.items"
              :missions="mission.items"
              :inventories="inventory.items"
              :recommendations="rec.items"
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
let dashboardWs = null

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

  map.setAgvs(agv.items)

  console.log('[MISSION ITEMS]', mission.items)

  console.log(
    '[AGV01 MISSIONS]',
    agv01Missions.value
  )

  console.log(
    '[AGV02 MISSIONS]',
    agv02Missions.value
  )
}

onMounted(async () => {

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
          map.updateAgv(msg.data)

          mission.load().catch(console.error)
          break

        case 'TASK_REFRESH':
          task.load()
            .then(() => console.log('[TASK AFTER LOAD]', task.items))
            .catch(console.error)
          break

        case 'MISSION_REFRESH':
          Promise.allSettled([
            mission.load(),
            agv.load(),
            map.load()
          ])
            .then(() => {
              map.setAgvs(agv.items)

              console.log('[MISSION AFTER LOAD]', mission.items)
              console.log('[AGV AFTER LOAD]', agv.items)
              console.log('[AGV01 AFTER LOAD]', agv01Missions.value)
              console.log('[AGV02 AFTER LOAD]', agv02Missions.value)
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
  dashboardWs?.close()
})
</script>