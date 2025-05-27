<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useBidStore } from '@/stores/bidStore'

const router = useRouter()
const bidStore = useBidStore()

const categories = ref([
  { id: 1, name: '경제 & 재정', icon: 'chart-line', count: 0 },
  { id: 2, name: '과학기술', icon: 'server', count: 0 },
  { id: 3, name: '교육 & 문화', icon: 'book', count: 0 },
  { id: 4, name: '국방 & 치안', icon: 'shield', count: 0 },
  { id: 5, name: '국토 & 교통', icon: 'building', count: 0 },
  { id: 6, name: '농림수산', icon: 'apple', count: 0 },
  { id: 7, name: '물류 & 외교', icon: 'truck', count: 0 },
  { id: 8, name: '보건 & 복지', icon: 'heart', count: 0 },
  { id: 9, name: '산업 & 에너지', icon: 'sun', count: 0 },
  { id: 10, name: '행정 & 법무', icon: 'briefcase', count: 0 },
])

function searchByCategory(categoryId) {
  // 조회 결과 페이지로 이동
  router.push({ name: 'Bids', query: { categoryId } })
}

onMounted(async () => {
  try {
    // console.log('🐛 공고 집계 요청')
    await bidStore.fetchBidCount()

    bidStore.bidCounts.forEach((bidCount) => {
      const category = categories.value.find((cat) => cat.id === Number(bidCount.majorCategoryCode))
      if (category) {
        category.count = bidCount.count
      }
    })
    // console.log('🐛 공고 집계 완료 : ', categories.value)
  } catch (error) {
    console.error(error)
  }
})
</script>

<template>
  <!-- 카테고리별 섹션 -->
  <div class="w-full mb-10 space-y-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold mb-2">분야별 입찰 공고</h1>
      <p class="text-gray-500">다양한 분야의 입찰 공고를 확인하세요.</p>
    </div>

    <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-5 gap-5">
      <div
        v-for="(category, index) in categories"
        :key="index"
        @click="searchByCategory(category.id)"
        class="cursor-pointer bg-white rounded-xl shadow hover:shadow-md transition-transform duration-300 ease-in-out hover:-translate-y-1 hover:scale-105 py-10 px-4 text-center"
      >
        <div class="mb-4 flex justify-center">
          <i class="!text-3xl text-primary-400" :class="`pi pi-${category.icon}`"></i>
        </div>
        <div class="text-lg font-semibold text-gray-800">{{ category.name }}</div>
        <div class="text-gray-500">{{ category.count }}건</div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
