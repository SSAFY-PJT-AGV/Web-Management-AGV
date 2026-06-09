import axios from 'axios'

export const http = axios.create({
  baseURL:
      import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',

  timeout: 2500,
})


// JWT
http.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken')

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})


// 응답 처리
function unwrap(response) {
  const body = response.data

  // 백엔드가 배열을 바로 반환하는 경우
  if (Array.isArray(body)) {
    return body
  }

  // 공통 응답 형식: { success, data, message, error }
  if (body?.success === true) {
    return body.data
  }

  if (body?.success === false) {
    throw body.error
  }

  // 단일 객체 응답
  return body
}


export const safeGet = async url => {
  try {
    const response = await http.get(url)
    return unwrap(response)

  } catch (error) {
    console.error('[API GET ERROR]', url, error)
    throw error
  }
}


export const safePost = async (url, body) => {
  try {
    const response = await http.post(url, body)
    return unwrap(response)

  } catch (error) {
    console.error('[API POST ERROR]', url, error)
    throw error
  }
}