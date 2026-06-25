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
              :agvs="displayAgvs"
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
      :events="event.items"
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
const map = useMapStore()
const task = useTaskStore()
const event = useEventStore()

const activeMissionStatuses = [
  'IN_PROGRESS',
  'ASSIGNED',
  'CREATED',
  'QUEUED',
  'WAITING'
]

const agv01MissionTypes = [
  'PICK_FROM_STORAGE',
  'DROP_TO_STORAGE',
  'DROP_TO_CONVEYOR',
  'PICK_FROM_CROSS'
]

const agv02MissionTypes = [
  'PICK_EMPTY_BOX',
  'DROP_EMPTY_BOX',
  'PICK_FROM_CONVEYOR',
  'DROP_TO_FINISHED_BOX_STORAGE',
  'PICK_FROM_FINISHED_BOX_STORAGE',
  'DROP_TO_OUTBOUND',
  'PICK_FROM_INBOUND',
  'DROP_TO_CROSS'
]

function missionSortValue(m) {
  return (
    m.sequenceOrder ??
    m.sequence_order ??
    m.sequence ??
    m.missionId ??
    m.commandId ??
    999999
  )
}

function missionBelongsToAgv(missionItem, agvId) {
  const missionAgvId =
    missionItem.agvId ??
    missionItem.agv?.agvId ??
    missionItem.assignedAgvId ??
    null

  if (missionAgvId !== null && missionAgvId !== undefined) {
    return String(missionAgvId) === String(agvId)
  }

  if (String(agvId) === '1') {
    return agv01MissionTypes.includes(missionItem.missionType)
  }

  if (String(agvId) === '2') {
    return agv02MissionTypes.includes(missionItem.missionType)
  }

  return false
}

function buildAgvMissions(agvId) {
  return mission.items
    .filter(m =>
      missionBelongsToAgv(m, agvId) &&
      activeMissionStatuses.includes(m.status)
    )
    .sort((a, b) => {
      const statusOrder = {
        IN_PROGRESS: 1,
        ASSIGNED: 2,
        WAITING: 3,
        QUEUED: 4,
        CREATED: 5
      }

      const diff =
        (statusOrder[a.status] ?? 99) -
        (statusOrder[b.status] ?? 99)

      if (diff !== 0) return diff

      return missionSortValue(a) - missionSortValue(b)
    })
}

const agv01Missions = computed(() => buildAgvMissions(1))
const agv02Missions = computed(() => buildAgvMissions(2))

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

const displayAgvs = computed(() =>
  agv.items.map(a => ({
    ...a,
    currentMarkerId:
      a.currentMarkerId ??
      a.current_marker_id ??
      a.currentMarker?.markerId ??
      a.located ??
      null
  }))
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
        case 'AGV_STATUS_UPDATED': {
          const agvData = {
            ...msg.data
          }

          agv.update(agvData)

          if (!agvData?.testMode) {
            map.updateAgv(agvData)
          }

          break
        }

        case 'TASK_REFRESH':
          refreshDashboard().catch(console.error)
          break

        case 'MISSION_REFRESH':
          mission.load().catch(console.error)
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
          map.load()
            .then(() => {
              map.setAgvs(displayAgvs.value)
            })
            .catch(console.error)
          break

        default:
          console.warn('[UNKNOWN DASHBOARD WS MESSAGE]', msg)
      }
    }
  })
})

onUnmounted(() => {
  clearInterval(clockTimer)
  dashboardWs?.close()
})
</script>