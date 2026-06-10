<template>
  <main class="grid-background h-screen overflow-hidden p-2">
    <div class="h-full flex flex-col gap-2">
      <HeaderStatus :connected="connected" />

      <section class="grid grid-cols-[29%_44%_25%] gap-2 flex-1 min-h-0 overflow-hidden">
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
                <MissionQueue :items="agv01Missions" />
              </div>
            </PanelFrame>

            <PanelFrame title="AGV02 MISSION QUEUE" class="min-h-0 overflow-hidden">
              <div class="h-full min-h-0 overflow-y-auto pr-1">
                <MissionQueue :items="agv02Missions" />
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

          <PanelFrame title="INVENTORY" class="flex-[1.1] min-h-0 overflow-hidden">
            <InventoryPanel :items="inventory.items" />
          </PanelFrame>

          <PanelFrame title="AI RECOMMENDATION" class="h-[160px] min-h-0 overflow-hidden relative">
            <div class="absolute inset-x-4 top-[54px] bottom-3 overflow-y-auto pr-2">
              <AiRecommendation :items="rec.items" />
            </div>
          </PanelFrame>

          <PanelFrame title="OUTBOUND QUEUE" class="flex-1 min-h-0 overflow-hidden">
            <div class="h-full min-h-0 overflow-y-auto pr-1">
              <OutboundQueue :items="task.items" />
            </div>
          </PanelFrame>
        </aside>
      </section>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'

import HeaderStatus from '../components/layout/HeaderStatus.vue'
import PanelFrame from '../components/layout/PanelFrame.vue'
import AgvStatusCard from '../components/agv/AgvStatusCard.vue'
import AgvControl from '../components/agv/AgvControl.vue'
import MissionQueue from '../components/mission/MissionQueue.vue'
import DispatchStatus from '../components/dispatch/DispatchStatus.vue'
import FactoryMap from '../components/map/FactoryMap.vue'
import CommandPanel from '../components/command/CommandPanel.vue'
import InventoryPanel from '../components/inventory/InventoryPanel.vue'
import AiRecommendation from '../components/ai/AiRecommendation.vue'
import EventLog from '../components/event/EventLog.vue'
import OutboundQueue from '../components/outbound/OutboundQueue.vue'
import { useTaskStore } from '../stores/taskStore'

import { useAgvStore } from '../stores/agvStore'
import { useMissionStore } from '../stores/missionStore'
import { useInventoryStore } from '../stores/inventoryStore'
import { useRecommendationStore } from '../stores/recommendationStore'
import { useEventStore } from '../stores/eventStore'
import { useMapStore } from '../stores/mapStore'

import { connectDashboardSocket } from '../websocket/dashboardSocket'

const connected = ref(false)
let dashboardWs = null

const agv = useAgvStore()
const mission = useMissionStore()
const inventory = useInventoryStore()
const rec = useRecommendationStore()
const event = useEventStore()
const map = useMapStore()
const task = useTaskStore()

const agv01Missions = computed(() =>
  mission.items.filter(m => String(m.agvId) === '1')
)

const agv02Missions = computed(() =>
  mission.items.filter(m => String(m.agvId) === '2')
)

onMounted(async () => {
  await Promise.allSettled([
    agv.load(),
    mission.load(),
    inventory.load(),
    map.load(),
    event.load(),
    rec.load(),
    task.load()
  ])

    // =========================
    // Mission Debug
    // =========================
    console.log('[MISSION ITEMS]', mission.items)

    console.log(
      '[AGV01 MISSIONS]',
      agv01Missions.value
    )

    console.log(
      '[AGV02 MISSIONS]',
      agv02Missions.value
    )

  map.setAgvs(agv.items)

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
          break

        case 'TASK_REFRESH':
          task.load()
            .then(() => console.log('[TASK AFTER LOAD]', task.items))
            .catch(console.error)
          break

        case 'MISSION_REFRESH':
          mission.load()
            .then(() => {
              console.log('[MISSION AFTER LOAD]', mission.items)
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