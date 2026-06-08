import { defineStore } from 'pinia'
import { mock } from './mockData'
import { inventoryApi } from '../api/inventoryApi'
export const useInventoryStore = defineStore('inventory',{state:()=>({items:[...mock.inventory]}),actions:{async load(){this.items=await inventoryApi.list()},setItems(v){this.items=v}}})
