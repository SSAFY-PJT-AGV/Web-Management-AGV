import { safeGet } from './http'

export const markerApi = {
  // Marker 목록 조회
  list: () =>
      safeGet('/api/markers'),

  // Factory Map 조회
  map: () =>
      safeGet('/api/markers/map')
}