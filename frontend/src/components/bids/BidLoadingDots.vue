<!-- components/BidLoadingDots.vue -->
<template>
  <div class="flex flex-col items-center justify-center min-h-[300px] text-primary-800 mb-16">
    <!-- 로고 -->
    <img src="@/assets/logo.png" alt="Logo" class="w-32 h-auto mb-4" />

    <!-- 점 애니메이션 -->
    <div class="text-7xl font-semibold tracking-widest font-mono h-6 mb-8" style="white-space: pre">
      {{ paddedDots }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const count = ref(1)

const paddedDots = computed(() => {
  const total = 5
  const dots = '.'.repeat(count.value)
  const spaces = ' '.repeat(total - count.value)
  return dots + spaces
})

let interval = null

onMounted(() => {
  interval = setInterval(() => {
    count.value = (count.value % 5) + 1
  }, 300)
})

onUnmounted(() => {
  if (interval) clearInterval(interval)
})
</script>
