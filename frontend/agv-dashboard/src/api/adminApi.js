import axios from 'axios'
import { safePost } from './http'

const BASE_URL =
    import.meta.env.VITE_API_BASE_URL || ''

export const adminApi = {

    resetDemo: body =>
        safePost(
            '/api/admin/demo/reset',
            body
        ),

    connectAgv: (agvId, body) =>
        safePost(
            `/api/admin/demo/agvs/${agvId}/connect`,
            body
        ),

    prepareScaleTest: body =>
        safePost(
            '/api/admin/scale-test/prepare',
            body
        ),

    cleanupScaleTest: () =>
        safePost(
            '/api/admin/scale-test/cleanup'
        ),

    analyzeScaleTest: (agv, fps) =>
        axios.get(
            `${BASE_URL}/api/scale/analyze?agv=${agv}&fps=${fps}`,
            {
                timeout: 180000
            }
        )
}