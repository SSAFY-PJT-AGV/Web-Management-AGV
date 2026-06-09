import { defineStore } from 'pinia'
import { inventoryApi } from '../api/inventoryApi'

export const useInventoryStore = defineStore('inventory', {
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
                this.items = await inventoryApi.list()
            } catch (e) {
                console.error(e)
                this.error = '재고 정보를 불러오지 못했습니다.'
            } finally {
                this.loading = false
            }
        },

        setItems(v) {
            this.items = v
        },
    },
})