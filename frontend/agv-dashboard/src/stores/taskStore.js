import { defineStore } from 'pinia'
import { taskApi } from '../api/taskApi'

export const useTaskStore = defineStore('task', {
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
                this.items = await taskApi.list()
            } catch (e) {
                console.error(e)
                this.error = '작업 목록 조회 실패'
            } finally {
                this.loading = false
            }
        },

        update(data) {
            const index = this.items.findIndex(
                task => String(task.taskId) === String(data.taskId)
            )

            if (index >= 0) {
                this.items[index] = {
                    ...this.items[index],
                    ...data,
                }
            } else {
                this.items.unshift(data)
            }
        },

        setItems(data) {
            this.items = data
        },
    },
})