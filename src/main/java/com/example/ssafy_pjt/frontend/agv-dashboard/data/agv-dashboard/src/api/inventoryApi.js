import { safeGet, safePost } from './http'
import { mock } from '../stores/mockData'

export const inventoryApi = {
  // 재고 목록 조회
  list: () =>
      safeGet('/api/inventories', mock.inventories || []),

  // 재고 보급 요청 생성
  // 프론트: { material: 'CHIP', quantity: 20 }
  // 백엔드: { materialCode: 'CHIP', quantity: 20 }
  createReplenishment: body =>
      safePost('/api/replenishments', {
        materialCode: body.material,
        quantity: body.quantity
      })
}