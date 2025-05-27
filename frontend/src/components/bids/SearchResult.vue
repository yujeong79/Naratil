<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useBidStore } from '@/stores/bidStore'
import dayjs from 'dayjs'

const router = useRouter()
const bidStore = useBidStore()
const bids = computed(() => bidStore.bids.map((bid, i) => ({ ...bid, idx: i + 1 })))

const formatDateTime = (datetime) => {
  if (!datetime) return '-'

  const parsed = dayjs(datetime)
  return parsed.isValid() ? parsed.format('YY-MM-DD HH:mm') : '-'
}

const formatBudget = (price) => {
  if (!price) return '미정'

  const budgetNum = parseFloat(price)
  if (isNaN(budgetNum)) return '미정'

  if (budgetNum >= 100000000) {
    return `${(budgetNum / 100000000).toFixed(1)}억원`
  } else if (budgetNum >= 10000) {
    return `${(budgetNum / 10000).toFixed(0)}만원`
  } else {
    return `${budgetNum.toLocaleString()}원`
  }
}

function handleDetail(bid) {
  router.push(`/bids/${bid._id}`)
}
</script>

<template>
  <!-- 검색 결과 -->
  <div class="max-w-[1160px] mx-auto mt-6 text-sm">
    <BidLoadingDots v-if="bidStore.loading" />
    <DataTable
      v-else
      :value="bids"
      paginator
      :rows="10"
      tableStyle="background-color: rgb(243 244 246 / var(--tw-bg-opacity, 1));min-width: 1000px"
      stripedRows
      responsiveLayout="scroll"
      class="custom-table"
    >
      <!-- 순서 -->
      <Column
        field="idx"
        style="width: 5%; padding: 6px 8px; text-align: center"
        headerStyle="background-color: rgb(243 244 246 / var(--tw-bg-opacity, 1));"
        headerClass="w-full"
      >
        <template #header>
          <div class="font-bold text-center w-full">순서</div>
        </template>
      </Column>

      <!-- 구분 -->
      <Column
        field="bsnsDivNm"
        style="width: 5%; padding: 6px 8px; text-align: center"
        headerStyle="background-color: rgb(243 244 246 / var(--tw-bg-opacity, 1));"
        headerClass="w-full"
      >
        <template #header>
          <div class="font-bold text-center w-full">구분</div>
        </template>
      </Column>

      <!-- 공고종류 -->
      <!-- <Column
        field="ntceKindNm"
        style="width: 10%; padding: 6px 8px; text-align: center"
        headerStyle="background-color: rgb(243 244 246 / var(--tw-bg-opacity, 1));"
        headerClass="w-full"
      >
        <template #header>
          <div class="font-bold text-center w-full">공고종류</div>
        </template>
      </Column> -->

      <!-- 공고번호 -->
      <Column
        style="width: 15%; padding: 6px 8px; text-align: center"
        headerStyle="background-color: rgb(243 244 246 / var(--tw-bg-opacity, 1));"
        headerClass="w-full"
      >
        <template #header>
          <div class="font-bold text-center w-full">공고번호</div>
        </template>
        <template #body="{ data }"> {{ data.bidNtceNo }}-{{ data.bidNtceOrd || '000' }} </template>
      </Column>

      <!-- 공고명 -->
      <Column
        field="bidNtceNm"
        style="width: 35%; padding: 6px 8px; text-align: left"
        headerStyle="background-color: rgb(243 244 246 / var(--tw-bg-opacity, 1));"
        headerClass="w-full"
      >
        <template #header>
          <div class="font-bold text-center w-full">공고명</div>
        </template>
        <template #body="{ data }">
          <span class="text-gray-500 text-xs">{{ data.dminsttNm }} </span>
          <span
            class="text-blue-600 hover:underline cursor-pointer line-clamp-1 break-words"
            style="width: 100%"
            :title="data.bidNtceNm"
            @click="handleDetail(data)"
          >
            {{ data.bidNtceNm }}
          </span>
        </template>
      </Column>

      <!-- 공고기관 / 수요기관 -->
      <!-- <Column
        style="width: 20%; padding: 6px 8px; text-align: center"
        bodyStyle="white-space: pre-line"
        headerStyle="background-color: rgb(243 244 246 / var(--tw-bg-opacity, 1));"
        headerClass="w-full"
      >
        <template #header>
          <div class="font-bold text-center w-full">공고기관<br />수요기관</div>
        </template>
        <template #body="{ data }"> {{ data.ntceInsttNm }}<br />{{ data.dminsttNm }} </template>
      </Column> -->

      <!-- 게시일시 / 마감일시 -->
      <Column
        style="width: 10%; padding: 6px 8px; text-align: center"
        bodyStyle="white-space: pre-line"
        headerStyle="background-color: rgb(243 244 246 / var(--tw-bg-opacity, 1));"
        headerClass="w-full"
      >
        <template #header>
          <div class="font-bold text-center w-full">게시일시<br />마감일시</div>
        </template>
        <template #body="{ data }">
          {{ formatDateTime(data.bidNtceDt) }}<br />{{ formatDateTime(data.bidClseDt) }}
        </template>
      </Column>

      <!-- 계약방법 -->
      <Column
        field="cntrctCnclsMthdNm"
        style="width: 10%; padding: 6px 8px; text-align: center"
        headerStyle="background-color: rgb(243 244 246 / var(--tw-bg-opacity, 1));"
        headerClass="w-full"
      >
        <template #header>
          <div class="font-bold text-center w-full">계약방법</div>
        </template>
      </Column>

      <!-- 추정금액 -->
      <Column
        style="width: 10%; padding: 6px 8px; text-align: center"
        headerStyle="background-color: rgb(243 244 246 / var(--tw-bg-opacity, 1));"
        headerClass="w-full"
      >
        <template #header>
          <div class="font-bold text-center w-full">추정금액</div>
        </template>
        <template #body="{ data }">
          {{ formatBudget(data.presmptPrce) }}
        </template>
      </Column>

      <!-- 데이터 없을 때 -->
      <template #empty>
        <div class="min-h-72 flex items-center justify-center text-center text-gray-500 py-10">
          검색 결과가 없습니다. 다른 옵션을 선택해보세요.
        </div>
      </template>
    </DataTable>
  </div>
</template>

<style scoped>
::v-deep(.p-paginator) {
  background-color: transparent !important;
}
</style>
