<script setup>
import SearchFilter from '@/components/bids/SearchFilter.vue'
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-4 py-6">
    <!-- 제목 -->
    <h1 class="text-3xl font-semibold text-gray-800 mb-6">입찰 공고 검색</h1>

    <!-- 검색 폼 -->
    <SearchFilter />

    <!-- 검색 결과 -->
    <SearchResult />
  </div>
</template>

<style scoped></style>
