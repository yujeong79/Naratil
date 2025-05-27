<script setup></script>

<template>
  <footer class="bg-gray-900 text-gray-300 py-10">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="grid grid-cols-1 md:grid-cols-[2fr_1fr_1fr] gap-8">
        <!-- 왼쪽 정보 영역 (2비율) -->
        <div>
          <h3 class="text-xl font-semibold text-white mb-2">나라장터 입찰정보 플랫폼</h3>
          <p class="text-sm leading-relaxed">
            공공데이터 포털의 나라장터 입찰공고정보서비스 API를 활용한<br />
            빅데이터 기반 입찰 정보 분석 및 추천 시스템
          </p>
        </div>

        <!-- 서비스 링크 (1비율) -->
        <div>
          <h4 class="text-lg font-medium text-white mb-2">서비스</h4>
          <ul class="space-y-1 text-sm">
            <li><router-link to="/" class="hover:text-white">홈</router-link></li>
            <li><router-link to="/bids" class="hover:text-white">검색</router-link></li>
            <li><router-link to="/" class="hover:text-white">이용안내</router-link></li>
          </ul>
        </div>

        <!-- 정보 링크 (1비율) -->
        <div>
          <h4 class="text-lg font-medium text-white mb-2">정보</h4>
          <ul class="space-y-1 text-sm">
            <li><router-link to="/" class="hover:text-white">이용약관</router-link></li>
            <li>
              <router-link to="/" class="hover:text-white">개인정보처리방침</router-link>
            </li>
          </ul>
        </div>
      </div>

      <!-- 하단 -->
      <div class="border-t border-gray-700 mt-10 pt-4 text-sm text-center text-gray-500">
        &copy; 2025 나라장터 입찰정보 플랫폼. All rights reserved.
      </div>
    </div>
  </footer>
</template>

<style scoped></style>
