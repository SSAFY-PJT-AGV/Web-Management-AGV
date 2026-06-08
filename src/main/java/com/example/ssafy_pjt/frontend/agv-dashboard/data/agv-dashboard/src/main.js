import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import './assets/dashboard.css'

createApp(App).use(createPinia()).mount('#app')
