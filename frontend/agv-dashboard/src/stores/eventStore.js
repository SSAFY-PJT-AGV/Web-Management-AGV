import { defineStore } from 'pinia'
import { eventApi } from '../api/eventApi'

export const useEventStore = defineStore('event', {
    state: () => ({
        items: [],
        error: null,
        loading: false,
    }),

    actions: {

        // 초기 REST 조회
        async load() {
            this.loading = true
            this.error = null

            try {
                this.items = await eventApi.list()
            } catch (e) {
                console.error(e)
                this.error = '이벤트 로그 조회 실패'
            } finally {
                this.loading = false
            }
        },


        // WebSocket EVENT_LOG_CREATED
        push(event) {
            this.items.unshift(event)

            // 최근 80개 유지
            this.items = this.items.slice(0, 80)
        },


        // 전체 갱신 필요할 때
        setItems(events) {
            this.items = events
        }

    }
})