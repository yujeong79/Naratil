<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useBidStore } from '@/stores/bidStore'
import { useUserStore } from '@/stores/userStore'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

dayjs.extend(utc)
dayjs.extend(timezone)
dayjs.tz.setDefault('Asia/Seoul')

const router = useRouter()
const bidStore = useBidStore()
const userStore = useUserStore()

const recommendedBiddings = computed(() => bidStore.recommendBids)
const isLoggedIn = computed(() => !!userStore.user && !!userStore.user.email)

const isUrgent = (deadline) => {
  const today = dayjs().tz()
  const end = dayjs(deadline).tz()
  const diff = end.diff(today, 'day')
  return diff >= 0 && diff <= 5
}

const goToDetail = (id) => {
  router.push(`/bids/${id}`)
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const parsed = dayjs(datetime)
  return parsed.isValid() ? parsed.format('YY-MM-DD HH:mm') : '-'
}
const formatPrice = (price) => {
  if (!price || isNaN(price)) return '-'
  return Number(price).toLocaleString() + '원'
}

onMounted(async () => {
  const token = localStorage.getItem('accessToken')
  if (token && !userStore.user?.email) {
    await userStore.fetchUserInfo()
  }
  if (token && userStore.user?.email) {
    await bidStore.fetchRecommendedBid()
  }
})
</script>

<template>
  <div class="w-full mb-10 space-y-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold mb-2">추천 입찰 공고</h1>
      <p class="text-gray-500">관심 분야 및 최근 검색에 기반한 맞춤형 입찰 공고입니다.</p>
    </div>

    <!-- 로그인 안 한 경우 -->
    <div v-if="!isLoggedIn" class="text-center py-10 bg-gray-50 rounded-xl border">
      <p class="text-gray-700 mb-4 text-lg">로그인 하고 추천 공고를 확인해보세요.</p>
    </div>

    <div v-else-if="!userStore.user.bizno" class="text-center py-10 bg-gray-50 rounded-xl border">
      <p class="text-gray-700 text-lg mb-8">
        기업정보를 등록하시면 맞춤 입찰 공고를 추천받을 수 있어요.
      </p>

      <Button
        label="기업정보 등록하러 가기"
        variant="outlined"
        @click="router.push('/register-corp')"
      />
    </div>

    <!-- 로그인 했지만 추천 공고가 없는 경우 -->
    <div
      v-else-if="recommendedBiddings.length === 0"
      class="text-center py-14 bg-gray-50 rounded-xl border"
    >
      <p class="text-gray-700 text-lg mb-8">추천공고가 없습니다.</p>
    </div>

    <!-- 추천 공고 있을 경우 -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5">
      <div
        v-for="(item, index) in recommendedBiddings"
        :key="index"
        class="bg-white rounded-xl shadow hover:shadow-md transition-transform hover:-translate-y-1 cursor-pointer relative p-5 h-full"
        @click="goToDetail(item.bidNtceId)"
      >
        <!-- 제목 -->
        <h3 class="text-base font-semibold mb-4 line-clamp-1 break-words">
          {{ item.bidNtceNm }}
        </h3>

        <div class="space-y-2 text-sm text-gray-600">
          <div class="flex items-center gap-2">
            <i class="pi pi-building text-gray-400"></i>
            <span class="truncate block w-full" :title="item.dminsttNm">
              {{ item.dminsttNm }}
            </span>
          </div>

          <div class="flex items-center gap-2">
            <i class="pi pi-calendar text-gray-400"></i>
            <span>게시일: {{ formatDateTime(item.bidNtceDt) }}</span>
          </div>

          <div class="flex items-center gap-2">
            <i class="pi pi-clock text-gray-400"></i>
            <span> 마감일: </span>
            <span :class="isUrgent(item.bidClseDt) ? 'text-red-600' : ''">
              {{ formatDateTime(item.bidClseDt) }}
            </span>

            <span
              v-if="isUrgent(item.bidClseDt)"
              class="ml-2 inline-block text-xs font-bold py-0.5 px-1.5 rounded-full bg-red-100 text-red-700"
            >
              임박
            </span>
          </div>
          <div class="flex items-center gap-2">
            <i class="pi pi-dollar text-gray-400"></i>
            <span>추정금액: {{ formatPrice(item.presmptPrce) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
