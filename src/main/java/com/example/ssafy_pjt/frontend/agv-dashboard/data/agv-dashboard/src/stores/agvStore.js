import { defineStore } from 'pinia'
import { mock } from './mockData'
import { agvApi } from '../api/agvApi'
export const useAgvStore = defineStore('agv',{state:()=>({items:[...mock.agvs]}),actions:{async load(){this.items=await agvApi.list()},update(data){const i=this.items.findIndex(a=>a.agvId===data.agvId); if(i>=0)this.items[i]={...this.items[i],...data}}}})
