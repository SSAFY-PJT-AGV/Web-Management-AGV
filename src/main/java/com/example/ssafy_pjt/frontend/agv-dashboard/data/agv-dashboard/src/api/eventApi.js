import { safeGet } from './http'
import { mock } from '../stores/mockData'

export const eventApi = {
  // 이벤트 로그 조회
  list: () =>
      safeGet('/api/events', mock.events || [])
}