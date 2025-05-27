import axios from 'axios'
import router from '@/router'
import { useUserStore } from '@/stores/userStore'
import { pinia } from '@/stores'

console.log('baseURL: ', import.meta.env.VITE_APP_API_BASE_URL)

const api = axios.create({
  baseURL: import.meta.env.VITE_APP_API_BASE_URL,
  withCredentials: true, // HttpOnly 쿠키 자동 포함
})

// 요청 인터셉터: 토큰이 있을 경우 Authorization 헤더 추가
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `${token}`
  }
  return config
})

// 응답 인터셉터: 인증 실패 시 토큰 제거 및 로그인 페이지로 이동
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      console.warn('인증 실패 또는 토큰 만료')

      // 🔥 accessToken 제거
      localStorage.removeItem('accessToken')

      // 🔥 userStore 초기화 (선택)
      const userStore = useUserStore(pinia)
      userStore.clear()

      // 로그인 페이지로 이동
      router.push('/login')
    }
    return Promise.reject(err)
  },
)

export default api
