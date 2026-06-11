const fakeAgvSockets = new Map()

export function connectFakeAgv(agvId) {
    const existing = fakeAgvSockets.get(agvId)

    if (
        existing &&
        (
            existing.readyState === WebSocket.OPEN ||
            existing.readyState === WebSocket.CONNECTING
        )
    ) {
        console.log(`[FAKE AGV${agvId}] already connected`)
        return
    }

    fakeAgvSockets.delete(agvId)

    const wsUrl =
        import.meta.env.VITE_AGV_WS_URL ||
        'ws://localhost:8080/ws/agv'

    const socket = new WebSocket(wsUrl)

    fakeAgvSockets.set(agvId, socket)

    socket.onopen = () => {
        console.log(`[FAKE AGV${agvId}] connected`)

        socket.send(JSON.stringify({
            agvId,
            timestamp: Math.floor(Date.now() / 1000),
            status: 'IDLE',
            event: 'NONE',
            taskId: null,
            commandId: null,
            located: null,
            destination: null,
            cargo: 'EMPTY',
            isCW: false,
            hasImage: false,
            image: null
        }))
    }

    socket.onmessage = event => {
        console.log(`[FAKE AGV${agvId}] command`, event.data)
    }

    socket.onclose = () => {
        console.log(`[FAKE AGV${agvId}] closed`)
        fakeAgvSockets.delete(agvId)
    }

    socket.onerror = error => {
        console.error(`[FAKE AGV${agvId}] error`, error)
        fakeAgvSockets.delete(agvId)
    }
}

export function disconnectFakeAgv(agvId) {
    const socket = fakeAgvSockets.get(agvId)

    if (!socket) {
        return
    }

    socket.close()
    fakeAgvSockets.delete(agvId)
}

export function disconnectAllFakeAgvs() {
    for (const [agvId, socket] of fakeAgvSockets.entries()) {
        try {
            socket.close()
        } catch (e) {
            console.warn(`[FAKE AGV${agvId}] close fail`, e)
        }
    }

    fakeAgvSockets.clear()
}