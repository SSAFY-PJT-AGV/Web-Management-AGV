import { defineStore } from 'pinia'
import { agvApi } from '../api/agvApi'

function normalizeAgv(agv) {
    return {
        ...agv,

        // 백엔드 필드
        currentMarkerId: agv.currentMarkerId ?? agv.currentMarker ?? agv.located ?? null,

        // 프론트 기존 컴포넌트 호환용
        currentMarker: agv.currentMarkerId ?? agv.currentMarker ?? agv.located ?? null,

        cargo: agv.cargoMaterialCode ?? agv.cargoType ?? 'NONE',
    }
}

export const useAgvStore = defineStore('agv', {
    state: () => ({
        items: [],
        error: null,
        loading: false,
    }),

    actions: {
        async load() {
            this.loading = true
            this.error = null

            try {
                const data = await agvApi.list()
                this.items = data.map(normalizeAgv)
            } catch (e) {
                console.error(e)
                this.error = 'AGV 상태를 불러오지 못했습니다.'
            } finally {
                this.loading = false
            }
        },

        update(data) {
            const normalized = normalizeAgv(data)

            const index = this.items.findIndex(
                agv => agv.agvId === normalized.agvId
            )

            if (index >= 0) {
                this.items[index] = {
                    ...this.items[index],
                    ...normalized,
                }
            } else {
                this.items.push(normalized)
            }
        },

        setItems(data) {
            this.items = data.map(normalizeAgv)
        },
    },
})