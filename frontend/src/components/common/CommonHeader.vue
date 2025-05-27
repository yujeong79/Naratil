<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { storeToRefs } from 'pinia'

const router = useRouter()
const userStore = useUserStore()
const { token } = storeToRefs(userStore)
const logout = async () => {
  try {
    await userStore.logout() // store에 logout 액션이 정의되어 있어야 함
    router.push('/')
  } catch (err) {
    console.error('로그아웃 실패:', err)
    alert('로그아웃 중 오류가 발생했습니다.')
  }
}
</script>

<template>
  <header class="bg-white shadow-md sticky top-0 z-50">
    <div class="relative max-w-[1160px] mx-auto px-4 py-3 flex items-center justify-between">
      <!-- 로고 -->
      <div class="cursor-pointer" @click="router.push('/')">
        <span class="text-3xl text-primary-500 logo-text">나랏일</span>
      </div>

      <!-- 네비게이션 (중앙 고정) -->
      <nav class="absolute left-1/2 transform -translate-x-1/2">
        <ul class="flex gap-6 text-gray-700 text-base">
          <li>
            <router-link
              to="/"
              class="hover:text-primary-500"
              exact-active-class="text-primary-500 border-b-2 border-primary-500 pb-1"
              >홈</router-link
            >
          </li>
          <li>
            <router-link
              to="/bids"
              class="hover:text-primary-500"
              exact-active-class="text-primary-500 border-b-2 border-primary-500 pb-1"
              >검색</router-link
            >
          </li>
        </ul>
      </nav>

      <!-- 유저 액션 -->
      <div class="flex gap-2 items-center">
        <template v-if="token">
          <Button
            label="마이페이지"
            size="small"
            variant="outlined"
            @click="router.push('/mypage')"
          />
          <Button
            label="로그아웃"
            variant="outlined"
            severity="danger"
            size="small"
            @click="logout"
          />
        </template>
        <template v-else>
          <Button
            label="로그인"
            size="small"
            severity="secondary"
            variant="outlined"
            @click="router.push('/login')"
            class="text-gray-500"
          />
          <Button
            label="회원가입"
            size="small"
            @click="router.push('/signup')"
            class="text-white"
          />
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.logo-text {
  font-family: 'SuseongHyejeong', sans-serif;
}
</style>
