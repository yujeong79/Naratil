<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()

const signupForm = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: '',
})

const errors = ref({
  email: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: '',
})

const validateForm = () => {
  let valid = true
  errors.value = {
    email: '',
    password: '',
    confirmPassword: '',
    name: '',
    phone: '',
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  const koreanNameRegex = /^[가-힣]{2,}$/
  const phoneRegex = /^\d+$/

  if (!signupForm.email) {
    errors.value.email = '이메일을 입력해주세요'
    valid = false
  } else if (!emailRegex.test(signupForm.email)) {
    errors.value.email = '유효한 이메일 형식이 아닙니다'
    valid = false
  }

  if (!signupForm.password || signupForm.password.length < 6) {
    errors.value.password = '비밀번호는 6자리 이상이어야 합니다'
    valid = false
  }
  if (signupForm.password !== signupForm.confirmPassword) {
    errors.value.confirmPassword = '비밀번호가 일치하지 않습니다'
    valid = false
  }

  if (!signupForm.name) {
    errors.value.name = '이름을 입력해주세요'
    valid = false
  } else if (!koreanNameRegex.test(signupForm.name)) {
    errors.value.name = '이름은 한글 2자 이상으로 입력해주세요'
    valid = false
  }

  if (!signupForm.phone) {
    errors.value.phone = '전화번호를 입력해주세요'
    valid = false
  } else if (!phoneRegex.test(signupForm.phone)) {
    errors.value.phone = '전화번호는 숫자만 입력해주세요'
    valid = false
  }

  return valid
}

const goToLogin = () => {
  router.push('/login')
}

const handleSubmit = async () => {
  if (!validateForm()) return

  const confirmResult = window.confirm('입력한 정보로 가입하시겠습니까?')
  if (!confirmResult) return

  const payload = {
    email: signupForm.email,
    password: signupForm.password,
    name: signupForm.name,
    phone: signupForm.phone,
  }

  try {
    await userStore.signup(payload)
    alert('회원가입이 완료되었습니다.')
    router.push('/login')
  } catch (e) {
    alert(userStore.error || '회원가입에 실패했습니다')
  }
}
</script>

<template>
  <div class="min-h-[calc(100vh-160px)] flex justify-center items-center px-5 py-10 bg-gray-100">
    <div class="w-full max-w-[1000px] flex flex-col items-center gap-8">
      <!-- 로고 및 타이틀 -->
      <div class="text-center">
        <img src="@/assets/logo.png" alt="Logo" class="h-16 mb-4 mx-auto" />
        <h1 class="text-3xl font-bold text-blue-500">나라장터 입찰정보 플랫폼</h1>
        <p class="text-lg text-gray-600 mt-2">새로운 계정을 생성하여 서비스를 이용하세요</p>
      </div>

      <!-- 회원가입 카드 -->
      <Card class="w-full max-w-3xl p-4">
        <template #title>
          <div class="mb-4">
            <h2 class="text-2xl font-semibold text-gray-800">계정 정보</h2>
            <p class="text-sm text-gray-500 mt-1">로그인에 사용할 계정 정보를 입력해주세요</p>
          </div>
        </template>

        <template #content>
          <form @submit.prevent="handleSubmit" class="space-y-4">
            <!-- 이메일 -->
            <div>
              <label for="email" class="block text-sm font-medium text-gray-700 mb-1">이메일</label>
              <InputText
                id="email"
                v-model="signupForm.email"
                class="w-full"
                placeholder="이메일 주소"
              />
              <small class="text-red-500">{{ errors.email }}</small>
            </div>

            <!-- 비밀번호 -->
            <div>
              <label for="password" class="block text-sm font-medium text-gray-700 mb-1"
                >비밀번호</label
              >
              <Password
                id="password"
                v-model="signupForm.password"
                :feedback="false"
                toggleMask
                class="w-full"
                inputClass="w-full"
                placeholder="비밀번호 (6자리 이상)"
              />
              <small class="text-red-500">{{ errors.password }}</small>
            </div>

            <!-- 비밀번호 확인 -->
            <div>
              <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-1"
                >비밀번호 확인</label
              >
              <Password
                id="confirmPassword"
                v-model="signupForm.confirmPassword"
                :feedback="false"
                toggleMask
                class="w-full"
                inputClass="w-full"
                placeholder="비밀번호 확인"
              />
              <small class="text-red-500">{{ errors.confirmPassword }}</small>
            </div>

            <!-- 이름 -->
            <div>
              <label for="name" class="block text-sm font-medium text-gray-700 mb-1">이름</label>
              <InputText id="name" v-model="signupForm.name" class="w-full" placeholder="이름" />
              <small class="text-red-500">{{ errors.name }}</small>
            </div>

            <!-- 전화번호 -->
            <div>
              <label for="phone" class="block text-sm font-medium text-gray-700 mb-1"
                >전화번호</label
              >
              <InputText
                id="phone"
                v-model="signupForm.phone"
                class="w-full"
                placeholder="전화번호 ( - 없이 숫자만 입력 )"
              />
              <small class="text-red-500">{{ errors.phone }}</small>
            </div>
            <div class="flex justify-between mt-6">
              <Button
                label="취소"
                @click="goToLogin"
                class="p-button-outlined"
                severity="secondary"
                size="small"
              />
              <Button
                label="가입하기"
                @click="handleSubmit"
                class="p-button-primary"
                size="small"
              />
            </div>
          </form>
        </template>
      </Card>

      <div class="text-center text-sm text-gray-600 mt-4">
        이미 계정이 있으신가요?
        <Button label="로그인" link class="text-blue-500" @click="goToLogin" />
      </div>
    </div>
  </div>
</template>

<style scoped></style>
