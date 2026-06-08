export function connectDashboardSocket(handlers){
  let ws
  try{ ws = new WebSocket(import.meta.env.VITE_DASHBOARD_WS || 'ws://localhost:8080/ws/dashboard') }catch{return null}
  ws.onopen=()=>handlers.onOpen?.()
  ws.onerror=()=>handlers.onError?.()
  ws.onclose=()=>handlers.onClose?.()
  ws.onmessage=e=>{ try{ const msg=JSON.parse(e.data); handlers.onMessage?.(msg)}catch{} }
  return ws
}
