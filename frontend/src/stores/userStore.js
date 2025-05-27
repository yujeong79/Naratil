import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'

export const useUserStore = defineStore('userStore', () => {
  const token = ref(localStorage.getItem('accessToken'))
  const user = ref(null)
  const loading = ref(false)
  const error = ref(null)

  const signup = async (payload) => {
    loading.value = true
    error.value = null
    try {
      const response = await api.post('/auth/signup', payload)
      user.value = response.data
      return response.data
    } catch (err) {
      console.error('회원가입 실패:', err)
      error.value = err.response?.data?.message || '회원가입 중 오류가 발생했습니다.'
      throw err
    } finally {
      loading.value = false
    }
  }

  const login = async (payload) => {
    loading.value = true
    error.value = null
    try {
      const response = await api.post('/auth/login', payload)
      token.value = response.data.accessToken
      localStorage.setItem('accessToken', token.value)
      await fetchUserInfo() // 로그인 후 사용자 정보 불러오기
      return response.data
    } catch (err) {
      const status = err.response?.status
      const message = err.response?.data?.message || '로그인 중 오류 발생'
      if (status === 401) {
        error.value = '아이디 또는 비밀번호가 일치하지 않습니다.'
      } else if (status === 500) {
        error.value = '서버 오류입니다. 잠시 후 다시 시도해주세요.'
      } else {
        error.value = message
      }
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchUserInfo = async () => {
    loading.value = true
    try {
      console.log('🐛 userStore:', localStorage.getItem('accessToken'))
      const response = await api.get('/user/info')
      user.value = response.data
    } catch (err) {
      console.warn('로그인된 사용자 정보 없음', err.message)
      user.value = null
    } finally {
      loading.value = false
    }
  }

  const registerCorp = async (companyInfo) => {
    try {
      const res = await api.post('/corp/signup', companyInfo)
      return res
    } catch (err) {
      console.error('기업정보 등록 실패:', err)
      throw err
    }
  }

  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('accessToken')
  }

  const clear = () => {
    localStorage.removeItem('accessToken')
    token.value = null
    user.value = null
  }

  return {
    token,
    user,
    loading,
    error,
    signup,
    login,
    fetchUserInfo,
    registerCorp,
    logout,
    clear,
  }
})
