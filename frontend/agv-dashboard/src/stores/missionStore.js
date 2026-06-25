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
                const data = await missionApi.list()
                this.setItems(data)

            } catch (e) {
                console.error(e)
                this.error = 'Mission Queue 조회 실패'

            } finally {
                this.loading = false
            }
        },


        setItems(data) {

            const newIds = data.map(m => m.missionId)

            // 삭제된 mission 제거
            this.items = this.items.filter(
                old => newIds.includes(old.missionId)
            )

            data.forEach((mission, index) => {

                const existing = this.items.find(
                    item => item.missionId === mission.missionId
                )

                const normalized = {
                    ...mission,
                    order:
                        mission.sequence ??
                        index + 1,
                }

                if (existing) {
                    Object.assign(existing, normalized)
                } else {
                    this.items.push(normalized)
                }
            })
        }

    },
})