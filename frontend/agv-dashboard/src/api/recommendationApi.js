import { safeGet } from './http'

export const recommendationApi = {

  // AI 추천 목록 조회
  list: () =>
      safeGet('/api/recommendations')

}