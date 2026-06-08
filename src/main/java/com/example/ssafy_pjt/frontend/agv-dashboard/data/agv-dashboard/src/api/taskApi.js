import { safeGet, safePost } from './http'
import { mock } from '../stores/mockData'

export const taskApi = {
  // 생산 요청 목록 조회
  list: () =>
      safeGet('/api/tasks', mock.tasks || []),

  // 제품 생산 요청 생성
  createTask: body =>
      safePost('/api/tasks', body)
}