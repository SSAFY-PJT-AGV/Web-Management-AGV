import { safeGet } from './http'

export const missionApi = {
  summary: () =>
      safeGet('/api/missions/summary'),
}