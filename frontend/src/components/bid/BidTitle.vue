<script setup>
defineProps({
  notice: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <div v-if="notice" class="flex flex-wrap justify-center items-center gap-2 mb-2">
    <span class="px-3 py-1 text-sm bg-blue-100 text-blue-700 rounded-full">{{
      notice.bsnsDivNm
    }}</span>
    <span class="px-3 py-1 text-sm bg-green-100 text-green-700 rounded-full">{{
      notice.ntceKindNm
    }}</span>
  </div>
  <h2 class="text-2xl font-bold text-gray-900 text-center">{{ notice.bidNtceNm }}</h2>
</template>

<style scoped></style>
