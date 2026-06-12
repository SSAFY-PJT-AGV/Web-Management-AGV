import { safePost } from './http'

export const adminApi = {

    resetDemo: body =>
        safePost('/api/admin/demo/reset', body),


    connectAgv: (agvId, body) =>
        safePost(`/api/admin/demo/agvs/${agvId}/connect`, body)

}