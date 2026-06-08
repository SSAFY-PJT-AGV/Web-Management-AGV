import { safeGet } from './http'
import { mock } from '../stores/mockData'

export const markerApi = {
  // Marker 목록 조회
  list: () =>
      safeGet('/api/markers', mock.markers || []),

  // Factory Map 조회
  map: () =>
      safeGet('/api/markers/map', mock.map)
}