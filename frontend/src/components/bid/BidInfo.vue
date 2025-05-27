<script setup>
defineProps({
  notice: {
    type: Object,
    required: true,
  },
})
function formatCurrency(value) {
  if (!value) return null
  return Number(value).toLocaleString('ko-KR') + '원'
}
</script>

<template>
  <div v-if="notice" class="grid grid-cols-1 md:grid-cols-3 gap-6">
    <!-- 기본 정보 테이블 -->
    <div class="w-full">
      <h3 class="text-xl font-bold text-gray-700 mb-2">기본 정보</h3>
      <table class="w-full text-sm text-left border border-gray-300">
        <tbody>
          <tr class="border-t border-gray-200">
            <td class="w-1/3 px-4 py-2 font-medium bg-gray-50 border-r">공고번호</td>
            <td class="px-4 py-2">{{ notice.bidNtceNo }}-{{ notice.bidNtceOrd }}</td>
          </tr>
          <tr class="border-t border-gray-200">
            <td class="px-4 py-2 font-medium bg-gray-50 border-r">계약방법</td>
            <td class="px-4 py-2">{{ notice.cntrctCnclsMthdNm }}</td>
          </tr>
          <tr class="border-t border-gray-200">
            <td class="px-4 py-2 font-medium bg-gray-50 border-r">업종</td>
            <td class="px-4 py-2">
              <template v-if="notice.indstrytyNm && notice.indstrytyCd">
                {{ notice.indstrytyNm }} ({{ notice.indstrytyCd }})
              </template>
              <template v-else> - </template>
            </td>
          </tr>
          <tr class="border-t border-gray-200">
            <td class="px-4 py-2 font-medium bg-gray-50 border-r">지역제한</td>
            <td class="px-4 py-2">{{ notice.prtcptPsblRgnNm || '-' }}</td>
          </tr>
          <tr class="border-t border-gray-200">
            <td class="px-4 py-2 font-medium bg-gray-50 border-r">공고원문</td>
            <td class="px-4 py-2">
              <a
                :href="notice.bidNtceDtlUrl"
                target="_blank"
                class="text-blue-600 underline hover:text-blue-800"
              >
                나라장터 상세보기
              </a>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 금액 정보 테이블 -->
    <div class="w-full">
      <h3 class="text-xl font-bold text-gray-700 mb-2">금액 및 평가 관련 정보</h3>
      <table class="w-full text-sm text-left border border-gray-300">
        <tbody>
          <tr class="border-t border-gray-200">
            <td class="px-4 py-2 font-medium bg-gray-50 border-r">추정가격</td>
            <td class="px-4 py-2">{{ formatCurrency(notice.presmptPrce) }}</td>
          </tr>
          <tr class="border-t border-gray-200">
            <td class="px-4 py-2 font-medium bg-gray-50 border-r">투찰 하한금</td>
            <td class="px-4 py-2">
              {{
                notice.sucsfbidLwltRate && notice.presmptPrce
                  ? formatCurrency(
                      Math.round(notice.presmptPrce * 1.1 * (notice.sucsfbidLwltRate / 100)),
                    )
                  : '-'
              }}
              ({{ notice.sucsfbidLwltRate ? notice.sucsfbidLwltRate + '%' : '-' }})
            </td>
          </tr>
          <tr class="border-t border-gray-200">
            <td class="w-2/5 px-4 py-2 font-medium bg-gray-50 border-r">기초금액</td>
            <td class="px-4 py-2">{{ formatCurrency(notice.bssamt) || '-' }}</td>
          </tr>
          <tr class="border-t border-gray-200">
            <td class="px-4 py-2 font-medium bg-gray-50 border-r">예가방법</td>
            <td class="px-4 py-2">{{ notice.prearngPrceDcsnMthdNm }}</td>
          </tr>
          <tr class="border-t border-gray-200">
            <td class="px-4 py-2 font-medium bg-gray-50 border-r">예가변동폭</td>
            <td class="px-4 py-2">
              <template
                v-if="notice.rsrvtnPrceRngBgnRate != null && notice.rsrvtnPrceRngEndRate != null"
              >
                {{ notice.rsrvtnPrceRngBgnRate }}% ~ {{ notice.rsrvtnPrceRngEndRate }}%
              </template>
              <template v-else> - </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 기관 정보 테이블 -->
    <div class="w-full">
      <h3 class="text-xl font-bold text-gray-700 mb-2">기관 정보</h3>
      <table class="w-full text-sm text-left border border-gray-300">
        <tbody>
          <tr class="border-t border-gray-200">
            <td class="px-4 py-2 font-medium bg-gray-50 border-r">공고기관</td>
            <td class="px-4 py-2">{{ notice.ntceInsttNm }}</td>
          </tr>
          <tr class="border-t border-gray-200">
            <td class="px-4 py-2 font-medium bg-gray-50 border-r">수요기관</td>
            <td class="px-4 py-2">{{ notice.dminsttNm }}</td>
          </tr>
          <tr class="border-t border-gray-200">
            <td class="w-1/3 px-4 py-2 font-medium bg-gray-50 border-r">담당자</td>
            <td class="px-4 py-2">{{ notice.ntceInsttOfclNm }}</td>
          </tr>
          <tr class="border-t border-gray-200">
            <td class="px-4 py-2 font-medium bg-gray-50 border-r">연락처</td>
            <td class="px-4 py-2">{{ notice.ntceInsttOfclTelNo }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
