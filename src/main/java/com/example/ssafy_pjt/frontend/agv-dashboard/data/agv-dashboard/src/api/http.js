import axios from 'axios'

export const http = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080', timeout: 2500 })
http.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
export const safeGet = async (url, fallback) => { try { return (await http.get(url)).data } catch { return fallback } }
export const safePost = async (url, body, fallback={success:true}) => { try { return (await http.post(url, body)).data } catch { return fallback } }
