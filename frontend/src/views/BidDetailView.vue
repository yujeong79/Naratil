<script setup>
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useBidStore } from '@/stores/bidStore'

const route = useRoute()
const bidStore = useBidStore()

onMounted(async () => {
  const bidNtceId = route.params.bidNtceId
  if (bidNtceId) {
    console.log('상세조회 요청')
    await bidStore.fetchBid(bidNtceId)
  }
})
</script>

<template>
  <div class="max-w-5xl mx-auto min-h-80">
    <!-- 로딩 중 -->
    <div v-if="bidStore.loading" class="text-center text-gray-500 py-20 flex justify-center">
      공고 정보를 불러오는 중입니다...
    </div>

    <!-- 에러 발생 -->
    <div v-else-if="bidStore.error" class="text-center text-red-500 py-20">
      공고 정보를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.
      <img src="@/assets/error.png" alt="error" class="h-96 mt-10 mx-auto" />
    </div>

    <!-- 데이터가 정상적으로 있는 경우 -->
    <template v-else-if="bidStore.bid">
      <div class="my-10">
        <BidTitle :notice="bidStore.bid" />
      </div>

      <div class="w-full mx-auto p-6 mb-10 bg-white shadow">
        <BidTimeline :notice="bidStore.bid" />
      </div>

      <div class="w-full mx-auto p-6 mb-10 bg-white shadow">
        <BidInfo :notice="bidStore.bid" />
      </div>

      <div class="w-full mx-auto p-6 mb-10 bg-white shadow">
        <SimilarAward />
      </div>
    </template>

    <!-- 데이터 없음  -->
    <div v-else class="text-center text-gray-500 py-20">해당 공고 정보를 찾을 수 없습니다.</div>
  </div>
</template>

<style scoped></style>
