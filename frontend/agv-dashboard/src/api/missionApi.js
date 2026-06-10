import { safeGet } from './http'

export const missionApi = {
  list: () =>
      safeGet('/api/missions'),

  summary: () =>
      safeGet('/api/missions/summary'),
}