import { safeGet } from './http'

export const markerApi = {
  getMap: () =>
      safeGet('/api/markers/map'),
}