import { safeGet } from './http'

export const eventApi = {
  // 이벤트 로그 조회
  list: () =>
      safeGet('/api/events')
}