import { defineStore } from 'pinia'
import { missionApi } from '../api/missionApi'

export const useMissionStore = defineStore('mission', {
    state: () => ({
        items: [],
        loading: false,
        error: null,
    }),


    actions: {

        // 초기 Mission Queue 조회
        async load() {
            this.loading = true
            this.error = null

            try {
                const data = await missionApi.queue()
                this.setItems(data)

            } catch (e) {
                console.error(e)
                this.error = 'Mission Queue 조회 실패'

            } finally {
                this.loading = false
            }
        },


        // REST / WebSocket 공통 갱신
        setItems(data) {
            this.items = data.map((mission, index) => ({
                ...mission,

                // 화면 표시 순서
                order:
                    mission.sequence ??
                    index + 1,
            }))
        }

    },
})