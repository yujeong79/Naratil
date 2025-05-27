<script setup></script>

<template>
  <div class="flex flex-col min-h-screen">
    <CommonHeader />

    <!-- 남은 공간을 모두 차지 -->
    <div class="flex-1 w-full bg-neutral-50">
      <RouterView />
    </div>

    <CommonFooter />
  </div>
</template>

<style scoped></style>
