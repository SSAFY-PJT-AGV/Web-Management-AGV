import { defineStore } from 'pinia'
import { agvApi } from '../api/agvApi'

function hasValue(value) {
    return value !== null && value !== undefined && value !== '' && value !== '-'
}

function pickFirstValue(...values) {
    return values.find(hasValue)
}

function normalizeAgv(agv) {
    const currentMarker = pickFirstValue(
        agv.currentMarkerId,
        agv.current_marker_id,
        agv.currentMarker,
        agv.located
    )

    const destinationMarker = pickFirstValue(
        agv.destinationMarkerId,
        agv.destination_marker_id,
        agv.destinationMarker,
        agv.destination,
        agv.targetMarkerId,
        agv.target_marker_id
    )

    const target = pickFirstValue(
        agv.targetZoneName,
        agv.target,
        agv.destinationZoneName
    )

    return {
        ...agv,

        currentMarkerId: currentMarker ?? null,
        currentMarker: currentMarker ?? null,

        destinationMarkerId: destinationMarker ?? null,
        destinationMarker: destinationMarker ?? null,
        destination: destinationMarker ?? null,

        cargo:
            pickFirstValue(
                agv.cargoMaterialCode,
                agv.cargoType,
                agv.cargo
            ) ?? 'NONE',

        target: target ?? '-',
    }
}

function mergeAgv(oldAgv, incomingAgv) {
    const normalized = normalizeAgv(incomingAgv)

    const incomingCurrentMarker = pickFirstValue(
        incomingAgv.currentMarkerId,
        incomingAgv.current_marker_id,
        incomingAgv.currentMarker,
        incomingAgv.located
    )

    const incomingDestinationMarker = pickFirstValue(
        incomingAgv.destinationMarkerId,
        incomingAgv.destination_marker_id,
        incomingAgv.destinationMarker,
        incomingAgv.destination,
        incomingAgv.targetMarkerId,
        incomingAgv.target_marker_id
    )

    const incomingTarget = pickFirstValue(
        incomingAgv.targetZoneName,
        incomingAgv.target,
        incomingAgv.destinationZoneName
    )

    const merged = {
        ...oldAgv,
        ...normalized,
    }

    if (!hasValue(incomingCurrentMarker)) {
        merged.currentMarkerId =
            oldAgv.currentMarkerId ??
            oldAgv.currentMarker ??
            null

        merged.currentMarker =
            oldAgv.currentMarker ??
            oldAgv.currentMarkerId ??
            null
    }

    if (!hasValue(incomingDestinationMarker)) {
        merged.destinationMarkerId =
            oldAgv.destinationMarkerId ??
            oldAgv.destinationMarker ??
            oldAgv.destination ??
            null

        merged.destinationMarker =
            oldAgv.destinationMarker ??
            oldAgv.destinationMarkerId ??
            oldAgv.destination ??
            null

        merged.destination =
            oldAgv.destination ??
            oldAgv.destinationMarkerId ??
            oldAgv.destinationMarker ??
            null
    }

    if (!hasValue(incomingTarget)) {
        merged.target = oldAgv.target ?? '-'
    }

    if (
        !hasValue(incomingAgv.cargoMaterialCode) &&
        !hasValue(incomingAgv.cargoType) &&
        !hasValue(incomingAgv.cargo)
    ) {
        merged.cargo = oldAgv.cargo ?? 'NONE'
    }

    return merged
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
            const agv = this.items.find(
                item => String(item.agvId) === String(data.agvId)
            )

            if (agv) {
                Object.assign(agv, mergeAgv(agv, data))
            } else {
                this.items.push(normalizeAgv(data))
            }
        },

        setItems(data) {
            this.items = data.map(normalizeAgv)
        },
    },
})