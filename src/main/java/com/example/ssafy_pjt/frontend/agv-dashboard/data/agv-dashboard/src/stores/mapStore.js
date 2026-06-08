import { defineStore } from 'pinia'
import { mock } from './mockData'
import { markerApi } from '../api/markerApi'
export const useMapStore = defineStore('map',{state:()=>({markers:[...mock.map.markers],agvs:[...mock.map.agvs]}),getters:{markerById:s=>id=>s.markers.find(m=>m.markerId===id)},actions:{async load(){const m=await markerApi.map();this.markers=m.markers;this.agvs=m.agvs},updateAgv(data){const i=this.agvs.findIndex(a=>a.agvId===data.agvId);const v={agvId:data.agvId,currentMarker:data.markerId||data.marker,status:data.status}; if(i>=0)this.agvs[i]={...this.agvs[i],...v}; else this.agvs.push(v)}}})
