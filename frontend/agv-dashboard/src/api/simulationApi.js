import axios from 'axios'
import { safeGet } from './http'

export const simulationApi = {
    getAgvs() {
        return safeGet('/simulation/agvs')
    },

    openGazebo() {
        return axios.post('http://127.0.0.1:5001/open-gazebo')
    },
}