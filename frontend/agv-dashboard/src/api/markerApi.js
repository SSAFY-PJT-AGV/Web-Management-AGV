import { safeGet } from './http'

export const markerApi = {
  map: () =>
      safeGet('/api/markers/map'),
}