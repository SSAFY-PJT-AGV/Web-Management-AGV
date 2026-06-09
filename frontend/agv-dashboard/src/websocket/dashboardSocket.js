export function connectDashboardSocket(handlers) {
  let ws = null

  try {
    ws = new WebSocket(
        import.meta.env.VITE_DASHBOARD_WS ||
        'ws://localhost:8080/ws/dashboard'
    )
  } catch (e) {
    console.error('[WS CREATE ERROR]', e)
    return null
  }


  ws.onopen = () => {
    console.log('[WS CONNECTED]')
    handlers.onOpen?.()
  }


  ws.onerror = error => {
    console.error('[WS ERROR]', error)
    handlers.onError?.(error)
  }


  ws.onclose = () => {
    console.warn('[WS CLOSED]')
    handlers.onClose?.()
  }


  ws.onmessage = event => {
    try {
      const message = JSON.parse(event.data)

      handlers.onMessage?.(message)

    } catch (e) {
      console.error(
          '[WS MESSAGE PARSE ERROR]',
          e
      )
    }
  }


  return ws
}