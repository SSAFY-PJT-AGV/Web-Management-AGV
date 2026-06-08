import { safeGet } from './http'
import { mock } from '../stores/mockData'

export const missionApi = {
  // Mission 전체 조회
  list: () =>
      safeGet('/api/missions', mock.missions || []),

  // Dashboard용 Mission Queue 조회
  // missionId, taskId, commandId, MissionType은 화면에 노출하지 않음
  summary: () =>
      safeGet('/api/missions/summary', mock.missions || [])
}