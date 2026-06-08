import { defineStore } from 'pinia'
import { mock } from './mockData'
export const resolveEventLevel = msg => { const s=JSON.stringify(msg).toUpperCase(); if(s.includes('CRITICAL')||s.includes('긴급'))return 'CRITICAL'; if(s.includes('ERROR')||s.includes('FAILED')||s.includes('실패'))return 'ERROR'; if(s.includes('LOW')||s.includes('WARN')||s.includes('부족'))return 'WARN'; return 'INFO'}
export const useEventStore = defineStore('event',{state:()=>({items:[...mock.events]}),actions:{push(e){this.items.unshift({time:new Date().toLocaleTimeString('ko-KR',{hour12:false}),level:e.level||resolveEventLevel(e),message:e.message||String(e)});this.items=this.items.slice(0,80)}}})
