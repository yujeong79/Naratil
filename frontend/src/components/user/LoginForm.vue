<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()

const loginForm = reactive({
  email: '',
  password: '',
  remember: false,
})

const errors = ref({
  email: '',
  password: '',
})

const validateForm = () => {
  let valid = true
  errors.value = {
    email: '',
    password: '',
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

  if (!loginForm.email) {
    errors.value.email = '이메일을 입력해주세요'
    valid = false
  } else if (!emailRegex.test(loginForm.email)) {
    errors.value.email = '유효한 이메일 형식이 아닙니다'
    valid = false
  }

  if (!loginForm.password || loginForm.password.length < 6) {
    errors.value.password = '비밀번호는 6자리 이상이어야 합니다'
    valid = false
  }

  return valid
}

const handleSubmit = async () => {
  if (!validateForm()) return

  try {
    const payload = {
      email: loginForm.email,
      password: loginForm.password,
    }

    await userStore.login(payload)

    router.push('/')
  } catch (e) {
    console.error('로그인 실패:', e.message)
    alert(userStore.error || '로그인에 실패했습니다.')
  }
}

const goToSignup = () => {
  router.push('/signup')
}
</script>

<template>
  <!-- 로고 및 타이틀 -->
  <div class="text-center">
    <img src="@/assets/logo.png" alt="Logo" class="h-16 mb-4 mx-auto" />
    <h1 class="text-3xl font-bold text-blue-500">나라장터 입찰정보 플랫폼</h1>
    <p class="text-lg text-gray-600 mt-2">나랏일에 오신 것을 환영합니다</p>
  </div>

  <!-- 로그인 카드 -->
  <Card class="w-full max-w-sm">
    <template #title>
      <div class="text-center border-b py-4 mb-4">
        <h2 class="text-2xl font-semibold text-gray-800">로그인</h2>
        <p class="text-sm text-gray-500 mt-1 mb-4">계정 정보를 입력하여 로그인하세요</p>
      </div>
    </template>

    <template #content>
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <!-- 이메일 -->
        <div>
          <label class="block mb-1 font-medium text-sm">이메일</label>
          <InputText
            v-model="loginForm.email"
            type="email"
            class="w-full"
            placeholder="이메일 주소 입력"
          />
          <small class="text-red-500">{{ errors.email }}</small>
        </div>

        <!-- 비밀번호 -->
        <div>
          <label class="block mb-1 font-medium text-sm">비밀번호</label>
          <Password
            v-model="loginForm.password"
            :feedback="false"
            inputClass="w-full"
            class="w-full"
            toggleMask
            placeholder="비밀번호 입력"
          />
          <small class="text-red-500">{{ errors.password }}</small>
        </div>

        <!-- 옵션 -->
        <!-- <div class="flex justify-between items-center">
          <div class="flex items-center gap-2">
            <Checkbox v-model="loginForm.remember" binary inputId="remember" />
            <label for="remember" class="text-sm">아이디 저장</label>
          </div>
          <a href="#" class="text-sm text-blue-500 hover:underline">비밀번호 찾기</a>
        </div> -->

        <!-- 로그인 버튼 -->
        <div>
          <Button label="로그인" type="submit" class="w-full mt-4" />
        </div>
      </form>

      <!-- 회원가입 -->
      <div class="text-center text-sm text-gray-600 mt-4">
        계정이 없으신가요?
        <Button label="회원가입" link class="text-blue-500" @click="goToSignup" />
      </div>
    </template>
  </Card>
</template>

<style scoped></style>
