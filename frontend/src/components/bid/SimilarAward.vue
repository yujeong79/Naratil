<script setup>
import Chart from 'primevue/chart'
import { useBidStore } from '@/stores/bidStore'
const bidStore = useBidStore()

const formatCurrency = (value) => Number(value).toLocaleString('ko-KR') + ' 원'

const getChartData = (bizList) => ({
  labels: bizList.map((b) => `${b.opengRank}순위`),
  datasets: [
    {
      label: '투찰 금액',
      data: bizList.map((b) => b.bidprcAmt),
      backgroundColor: bizList.map(
        (b) => (b.opengRank === '1' ? '#3b82f6' : '#c7c7c7'), // 1순위 파랑, 나머지 회색
      ),
    },
  ],
})

const getBidAmountRangeForChart = (bizList) => {
  const amounts = bizList.map((b) => b.bidprcAmt)
  const min = Math.min(...amounts)
  const max = Math.max(...amounts)

  return {
    min: Math.floor(min * 0.99),
    max: Math.ceil(max * 1.01),
  }
}

const getChartOptions = (bizList, yMin, yMax, chartTitle = '낙찰 금액 5순위') => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    title: {
      display: true,
      text: chartTitle,
      position: 'bottom',
      align: 'center',
      font: { size: 14, weight: 'bold' },
      padding: { top: 10 },
      color: '#374151',
    },
    tooltip: {
      callbacks: {
        label: (ctx) => {
          const b = bizList[ctx.dataIndex]
          const lines = []

          lines.push(`업체명: ${b.prcbdrNm || '-'}`)

          if (b.bidprcAmt !== null) {
            lines.push(`입찰참여금액: ${formatCurrency(b.bidprcAmt)}`)
          } else {
            lines.push(`입찰참여금액: -`)
          }

          if (b.bidprcrt !== null) {
            lines.push(`투찰률: ${b.bidprcrt}%`)
          } else {
            lines.push(`투찰률: -`)
          }

          lines.push(`비고: ${b.rmrk || '-'}`)

          if (b.emplyeNum !== null) {
            lines.push(`종업원수: ${b.emplyeNum}명`)
          } else {
            lines.push(`종업원수: -`)
          }

          lines.push(`업종코드: ${b.indstrytyCd || '-'}`)

          if (b.presmptPrce !== null) {
            lines.push(`최근 참여 사업 금액: ${formatCurrency(b.presmptPrce)}`)
          } else {
            lines.push(`최근 참여 사업 금액: -`)
          }

          return lines
        },
      },
    },
  },
  scales: {
    x: { ticks: { display: false }, grid: { display: false } },
    y: {
      min: yMin,
      max: yMax,
      ticks: { display: false },
      grid: { display: false },
    },
  },
  layout: { padding: 0 },
  elements: {
    bar: {
      borderRadius: 4,
      borderSkipped: false,
    },
  },
})
</script>

<template>
  <div class="space-y-6">
    <h3 class="text-xl font-bold text-gray-800">과거 유사한 공고의 입찰 결과</h3>
    <div v-if="!bidStore.awards.length" class="text-gray-500 text-center py-20">
      유사 공고가 없습니다.
    </div>
    <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div
        v-for="(item, idx) in bidStore.awards"
        :key="idx"
        class="bg-gray-50 p-4 rounded-xl shadow space-y-4 flex flex-col h-full min-h-[280px]"
      >
        <!-- 제목 -->
        <div class="text-lg font-semibold text-gray-900">
          {{ item.bidNtceNm }}
        </div>

        <!-- 본문 정보 (1순위 낙찰자 정보) -->
        <template v-if="item.bizs_parsed?.length">
          <div class="text-sm text-gray-600 truncate">
            추정가격:
            <span class="font-medium">{{ formatCurrency(item.presmptPrce) }}</span>
          </div>
          <div class="text-sm text-gray-600 truncate">
            투찰 하한 금액:
            <span class="font-medium">
              {{
                item.presmptPrce && item.sucsfbidLwltRate
                  ? formatCurrency(
                      Math.round(item.presmptPrce * 1.1 * (item.sucsfbidLwltRate / 100)),
                    )
                  : '-'
              }}
              ( {{ item.sucsfbidLwltRate }}% )
            </span>
          </div>
          <!-- <div class="text-sm text-gray-600 truncate">
            투찰 하한율:
            <span class="font-medium"></span>
          </div> -->

          <div class="text-sm text-gray-600 truncate">
            낙찰기업:
            <span class="font-medium text-gray-800">{{ item.bizs_parsed[0].prcbdrNm }}</span>
          </div>
          <div
            class="text-sm text-gray-600 truncate"
            :title="formatCurrency(item.bizs_parsed[0].bidprcAmt)"
          >
            <div v-if="item.bizs_parsed[0].bidprcAmt !== null">
              낙찰금액: {{ formatCurrency(item.bizs_parsed[0].bidprcAmt) }} (
              {{ item.bizs_parsed[0].bidprcrt }} % )
            </div>
            <div v-else>낙찰금액: 데이터 없음</div>
          </div>
        </template>

        <!-- 차트 (맨 아래 정렬) -->
        <div class="mt-auto">
          <Chart
            :type="'bar'"
            :data="getChartData(item.bizs_parsed)"
            :options="
              getChartOptions(
                item.bizs_parsed,
                getBidAmountRangeForChart(item.bizs_parsed).min,
                getBidAmountRangeForChart(item.bizs_parsed).max,
              )
            "
            style="height: 160px"
          />
        </div>
      </div>
    </div>
  </div>
</template>
