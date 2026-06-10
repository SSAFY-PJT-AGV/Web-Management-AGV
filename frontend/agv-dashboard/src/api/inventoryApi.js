import { safeGet, safePost } from './http'

export const inventoryApi = {

    // 재고 목록 조회
    list: () =>
        safeGet('/api/inventories'),


    // 재고 보급 요청
    createReplenishment: body =>
        safePost(
            '/api/inventories/replenishment',
            body
        )

}