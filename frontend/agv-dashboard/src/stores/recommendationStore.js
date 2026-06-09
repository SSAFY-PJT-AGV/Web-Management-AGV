import { defineStore } from 'pinia'
import { recommendationApi } from '../api/recommendationApi'

export const useRecommendationStore = defineStore('recommendation', {
    state: () => ({
        items: [],
        loading: false,
        error: null,
    }),

    actions: {
        async load() {
            this.loading = true
            this.error = null

            try {
                this.items = await recommendationApi.list()
            } catch (e) {
                console.error(e)
                this.error = 'AI 추천 목록 조회 실패'
            } finally {
                this.loading = false
            }
        },

        setItems(v) {
            this.items = v
        },
    },
})