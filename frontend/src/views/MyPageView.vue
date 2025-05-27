<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()

const user = computed(() => userStore.user)

onMounted(async () => {
  await userStore.fetchUserInfo()
})
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8 space-y-10">
    <h1 class="text-3xl font-semibold text-gray-800 mb-6">마이페이지</h1>

    <!-- 로딩 또는 사용자 정보 없는 경우 -->
    <div v-if="!user">
      <p class="text-gray-500">사용자 정보를 불러오는 중입니다...</p>
    </div>

    <!-- 사용자 정보 -->
    <div v-else>
      <section class="bg-white border border-gray-200 p-6 shadow-sm">
        <h2 class="text-xl font-semibold text-gray-800 mb-6 border-b pb-2">사용자 정보</h2>
        <div class="grid grid-cols-1 gap-6">
          <div>
            <label class="block text-sm text-gray-600 mb-1">이메일</label>
            <div
              class="text-base text-gray-900 border border-gray-300 rounded-lg px-3 py-2 bg-gray-50"
            >
              {{ user.email }}
            </div>
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1">이름</label>
            <div
              class="text-base text-gray-900 border border-gray-300 rounded-lg px-3 py-2 bg-gray-50"
            >
              {{ user.name }}
            </div>
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1">전화번호</label>
            <div
              class="text-base text-gray-900 border border-gray-300 rounded-lg px-3 py-2 bg-gray-50"
            >
              {{ user.phone }}
            </div>
          </div>
        </div>
      </section>

      <!-- 기업 정보 -->
      <section class="bg-white border border-gray-200 p-6 shadow-sm">
        <h2 class="text-xl font-semibold text-gray-800 mb-6 border-b pb-2">기업 정보</h2>

        <!-- 기업 정보가 없는 경우 버튼 표시 -->
        <div v-if="!user.bizno" class="text-center">
          <Button label="기업정보 등록" variant="outlined" @click="router.push('/register-corp')" />
        </div>

        <div v-else class="grid grid-cols-1 gap-6">
          <div>
            <label class="block text-sm text-gray-600 mb-1">사업자번호</label>
            <div
              class="text-base text-gray-900 border border-gray-300 rounded-lg px-3 py-2 bg-gray-50"
            >
              {{ user.bizno }}
            </div>
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1">회사명</label>
            <div
              class="text-base text-gray-900 border border-gray-300 rounded-lg px-3 py-2 bg-gray-50"
            >
              {{ user.corpName }}
            </div>
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1">대표명</label>
            <div
              class="text-base text-gray-900 border border-gray-300 rounded-lg px-3 py-2 bg-gray-50"
            >
              {{ user.ceoName }}
            </div>
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1">개업일</label>
            <div
              class="text-base text-gray-900 border border-gray-300 rounded-lg px-3 py-2 bg-gray-50"
            >
              {{ user.openDate }}
            </div>
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1">종업원수</label>
            <div
              class="text-base text-gray-900 border border-gray-300 rounded-lg px-3 py-2 bg-gray-50"
            >
              {{ user.employeeCount }}
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
