import { safeGet } from './http'
import { mock } from '../stores/mockData'

export const recommendationApi = {
  // AI 추천 목록 조회
  list: () =>
      safeGet('/api/recommendations', mock.recommendations || [])
}