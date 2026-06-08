import { defineStore } from 'pinia'
import { mock } from './mockData'
export const useRecommendationStore = defineStore('recommendation',{state:()=>({items:[...mock.recommendations]}),actions:{setItems(v){this.items=v}}})
