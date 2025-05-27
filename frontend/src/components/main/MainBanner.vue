<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const searchKeyword = ref('')
const router = useRouter()

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      name: 'Bids',
      query: { keyword: searchKeyword.value },
    })
  }
}
</script>

<template>
  <div class="w-full my-10">
    <!-- 메인 배너 영역 -->
    <div class="bg-gradient-to-br from-blue-500 to-cyan-400 text-white rounded-xl p-10 mb-10">
      <div class="max-w-5xl">
        <h1 class="text-4xl font-bold mb-4">나라장터 입찰정보 빅데이터 플랫폼</h1>
        <p class="text-lg opacity-90 mb-6">
          공공데이터 기반 입찰 정보를 빠르게 검색하고 맞춤형 추천을 받아보세요.
        </p>

        <!-- 검색창 -->
        <div class="w-full max-w-4xl">
          <InputGroup class="h-10">
            <InputText
              v-model="searchKeyword"
              placeholder="입찰 공고, 사업명 등을 검색하세요"
              class="w-full"
              size="small"
              @keyup.enter="handleSearch"
            />
            <InputGroupAddon>
              <Button
                icon="pi pi-search"
                @click="handleSearch"
                severity="secondary"
                variant="text"
                class="!bg-transparent !shadow-none !transition-none hover:!bg-transparent focus:!ring-0 focus:!shadow-none active:!bg-transparent"
              />
            </InputGroupAddon>
          </InputGroup>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
