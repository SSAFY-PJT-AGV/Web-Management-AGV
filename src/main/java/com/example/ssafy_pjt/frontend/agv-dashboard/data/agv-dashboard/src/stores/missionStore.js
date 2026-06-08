import { defineStore } from 'pinia'
import { mock } from './mockData'
import { missionApi } from '../api/missionApi'
export const useMissionStore = defineStore('mission',{state:()=>({items:[...mock.missions]}),actions:{async load(){this.items=await missionApi.summary()},setItems(v){this.items=v.map((x,i)=>({...x,order:x.order??i+1}))}}})
