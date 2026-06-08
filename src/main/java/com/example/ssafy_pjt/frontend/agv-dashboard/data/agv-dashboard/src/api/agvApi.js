import { safeGet, safePost } from './http'
import { mock } from '../stores/mockData'

export const agvApi = {
  // AGV 상태 목록 조회
  list: () =>
      safeGet('/api/agvs', mock.agvs || []),

  // AGV 일시정지
  pause: id =>
      safePost(`/api/agvs/${id}/pause`, {}),

  // AGV 재개
  resume: id =>
      safePost(`/api/agvs/${id}/resume`, {}),

  // AGV 현재 작업 취소
  cancel: id =>
      safePost(`/api/agvs/${id}/cancel-current-task`, {})
}