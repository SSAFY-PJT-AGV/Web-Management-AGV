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
            state.markers.find(
                marker => marker.markerId === id
            ),
    },


    actions: {

        async load() {
            this.loading = true
            this.error = null

            try {
                const data = await markerApi.list()

                this.markers = data.markers ?? []
                this.agvs = data.agvs ?? []

            } catch (e) {
                console.error(e)
                this.error = '맵 정보를 불러오지 못했습니다.'

            } finally {
                this.loading = false
            }
        },


        updateAgv(data) {

            const markerId =
                data.currentMarkerId ??
                data.currentMarker ??
                data.markerId ??
                data.located ??
                null


            const index = this.agvs.findIndex(
                agv =>
                    String(agv.agvId)
                        .replace('AGV0', '') ===
                    String(data.agvId)
            )


            const value = {
                agvId:
                    typeof data.agvId === 'number'
                        ? `AGV0${data.agvId}`
                        : data.agvId,

                currentMarker: markerId,
                currentMarkerId: markerId,
                status: data.status,
            }


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
                    agvId:
                        typeof agv.agvId === 'number'
                            ? `AGV0${agv.agvId}`
                            : agv.agvId,

                    currentMarker: markerId,
                    currentMarkerId: markerId,
                    status: agv.status,
                }
            })
        },
    },
})