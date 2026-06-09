import { defineStore } from 'pinia'
import { markerApi } from '../api/markerApi'

export const useMapStore = defineStore('map', {
    state: () => ({
        markers: [],
        agvs: [],
        loading: false,
        error: null,
    }),

    getters: {
        markerById: state => id =>
            state.markers.find(marker => marker.markerId === id),
    },

    actions: {
        async load() {
            this.loading = true
            this.error = null

            try {
                this.markers = await markerApi.list()
            } catch (e) {
                console.error(e)
                this.error = '마커 정보를 불러오지 못했습니다.'
            } finally {
                this.loading = false
            }
        },

        updateAgv(data) {
            const markerId =
                data.currentMarkerId ??
                data.currentMarker ??
                data.markerId ??
                data.marker ??
                data.located ??
                null

            const value = {
                agvId: data.agvId,
                currentMarker: markerId,
                currentMarkerId: markerId,
                status: data.status,
            }

            const index = this.agvs.findIndex(
                agv => agv.agvId === data.agvId
            )

            if (index >= 0) {
                this.agvs[index] = {
                    ...this.agvs[index],
                    ...value,
                }
            } else {
                this.agvs.push(value)
            }
        },

        setAgvs(agvs) {
            this.agvs = agvs.map(agv => {
                const markerId =
                    agv.currentMarkerId ??
                    agv.currentMarker ??
                    agv.located ??
                    null

                return {
                    agvId: agv.agvId,
                    currentMarker: markerId,
                    currentMarkerId: markerId,
                    status: agv.status,
                }
            })
        },
    },
})