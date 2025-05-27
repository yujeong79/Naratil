<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useBidStore } from '@/stores/bidStore'
import { useRoute } from 'vue-router'
import { useRegions } from '@/composables/useRegions'
import dayjs from 'dayjs'

const route = useRoute()
const bidStore = useBidStore()

// 업종
const categoryOptions = [
  { label: '경제 & 재정', value: 1 },
  { label: '과학기술', value: 2 },
  { label: '교육 & 문화', value: 3 },
  { label: '국방 & 치안', value: 4 },
  { label: '국토 & 교통', value: 5 },
  { label: '농림수산', value: 6 },
  { label: '물류 & 외교', value: 7 },
  { label: '보건 & 복지', value: 8 },
  { label: '산업 & 에너지', value: 9 },
  { label: '행정 & 법무', value: 10 },
]
// 업무 구분
const bidDivOptions = [
  { label: '물품', value: '물품' },
  { label: '용역', value: '용역' },
  { label: '공사', value: '공사' },
]
const orgTypeOptions = [
  { label: '공고', value: 'notice' },
  { label: '수요', value: 'demand' },
]

// 지역
const { regions, selectedSido, selectedSigungu, sigunguList } = useRegions()

const sidoList = computed(() =>
  regions.value.map((r) => ({
    label: r.sido,
    value: r.sido,
  })),
)
const sigunguSelectList = computed(() => sigunguList.value)

watch(selectedSido, () => {
  selectedSigungu.value = null
})

const showAdvancedSearch = ref(false)
const selectedCategory = ref()
const selectedBidDiv = ref(null)

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : ''
}

const searchForm = ref({
  bidNtceNo: '',
  bidNtceNm: '',
  minBudget: null,
  maxBudget: null,
  startDate: null,
  endDate: null,
  orgType: 'demand',
  orgName: '',
})

const handleReset = () => {
  selectedSido.value = null
  selectedSigungu.value = null
  selectedCategory.value = null
  selectedBidDiv.value = null

  searchForm.value = {
    bidNtceNo: '',
    bidNtceNm: '',
    minBudget: null,
    maxBudget: null,
    startDate: null,
    endDate: null,
    orgType: 'demand',
    orgName: '',
  }
}

const handleSearch = async () => {
  const params = {
    bidNum: searchForm.value.bidNtceNo || undefined,
    bidName: searchForm.value.bidNtceNm || undefined,
    taskType: selectedBidDiv.value || undefined,
    indstryty: selectedCategory.value != null ? Number(selectedCategory.value) : undefined,
    region: selectedSido.value
      ? `${selectedSido.value} ${selectedSigungu.value || ''}`.trim()
      : undefined,
    ntceInsttNm: searchForm.value.orgType === 'notice' ? searchForm.value.orgName : undefined,
    dminsttNm: searchForm.value.orgType === 'demand' ? searchForm.value.orgName : undefined,
    minDate: searchForm.value.startDate ? formatDate(searchForm.value.startDate) : undefined,
    maxDate: searchForm.value.endDate ? formatDate(searchForm.value.endDate) : undefined,
    minPresmptPrce: searchForm.value.minBudget * 10000 || undefined,
    maxPresmptPrce: searchForm.value.maxBudget * 10000 || undefined,
  }

  await bidStore.fetchBids(params)
}

onMounted(async () => {
  const query = route.query

  if (query.categoryId) {
    selectedCategory.value = Number(query.categoryId)
  }

  if (query.keyword) {
    searchForm.value.bidNtceNm = query.keyword
  }

  await handleSearch()
})
</script>

<template>
  <div class="p-4 rounded-lg shadow space-y-4">
    <!-- 기본 옵션 -->
    <div class="flex flex-col md:flex-row items-center gap-8">
      <!-- 업종 -->
      <div class="flex items-center w-full md:w-1/3 gap-4">
        <label class="text-md font-medium text-gray-700 whitespace-nowrap">업종</label>
        <Select
          v-model="selectedCategory"
          :options="categoryOptions"
          optionLabel="label"
          optionValue="value"
          placeholder="업종 선택"
          showClear
          class="w-full"
          size="small"
        />
      </div>

      <!-- 공고명 -->
      <div class="flex items-center w-full md:w-2/3 gap-4">
        <label class="text-md font-medium text-gray-700 whitespace-nowrap">공고명</label>
        <InputText
          v-model="searchForm.bidNtceNm"
          placeholder="공고명"
          class="w-full h-[32px] text-sm"
        />
      </div>

      <!-- 검색 버튼 -->
      <div class="flex-shrink-0">
        <Button
          label="검색"
          icon="pi pi-search"
          class="min-w-[100px] h-[32px]"
          @click="handleSearch"
          size="small"
        />
      </div>
    </div>

    <!-- 고급 옵션 -->
    <Transition name="fade-slide">
      <div v-if="showAdvancedSearch">
        <Divider />
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8 mb-6">
          <!-- 1줄: 업무구분 -->
          <div>
            <label class="block text-md font-medium text-gray-700 mb-2">업무 구분</label>
            <Select
              v-model="selectedBidDiv"
              :options="bidDivOptions"
              optionLabel="label"
              optionValue="value"
              placeholder="업무 구분 선택"
              showClear
              class="w-full"
              size="small"
            />
          </div>

          <!-- 1줄: 공고번호 -->
          <div>
            <label class="block text-md font-medium text-gray-700 mb-2">공고번호</label>
            <InputText
              v-model="searchForm.bidNtceNo"
              placeholder="공고번호"
              class="w-full h-[32px] text-sm"
            />
          </div>

          <!-- 1줄: 기관 선택 -->
          <div class="md:col-span-2">
            <label class="block text-md font-medium text-gray-700 mb-2">기관명</label>
            <div class="flex gap-2">
              <SelectButton
                v-model="searchForm.orgType"
                :options="orgTypeOptions"
                optionLabel="label"
                optionValue="value"
                class="w-36 h-[32px] text-sm"
              />
              <InputText
                v-model="searchForm.orgName"
                :placeholder="
                  searchForm.orgType === 'notice' ? '공고기관명 입력' : '수요기관명 입력'
                "
                class="flex-1 h-[32px] text-sm"
              />
            </div>
          </div>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <!-- 2줄: 등록 일자 -->
          <div>
            <label class="block text-md font-medium text-gray-700 mb-2">등록 일자</label>
            <div class="flex gap-2">
              <Calendar
                v-model="searchForm.startDate"
                placeholder="시작일"
                dateFormat="yy-mm-dd"
                showIcon
                class="w-1/2 h-[32px] text-sm"
              />
              <Calendar
                v-model="searchForm.endDate"
                placeholder="종료일"
                dateFormat="yy-mm-dd"
                showIcon
                class="w-1/2 h-[32px] text-sm"
              />
            </div>
          </div>

          <!-- 2줄: 예산 범위 -->
          <div>
            <label class="block text-md font-medium text-gray-700 mb-2">예산 범위</label>
            <div class="flex gap-2">
              <InputNumber
                v-model="searchForm.minBudget"
                inputClass="w-full"
                placeholder="최소 금액"
                :min="0"
                mode="decimal"
                suffix=" 만원"
                class="w-1/2 h-[32px] text-sm"
              />
              <InputNumber
                v-model="searchForm.maxBudget"
                inputClass="w-full"
                placeholder="최대 금액"
                :min="0"
                mode="decimal"
                suffix=" 만원"
                class="w-1/2 h-[32px] text-sm"
              />
            </div>
          </div>

          <!-- 2줄: 지역 -->
          <div>
            <label class="block text-md font-medium text-gray-700 mb-2">지역</label>
            <div class="flex gap-2">
              <Select
                v-model="selectedSido"
                :options="sidoList"
                optionLabel="label"
                optionValue="value"
                placeholder="시/도 선택"
                showClear
                class="w-1/2"
                size="small"
              />
              <Select
                v-model="selectedSigungu"
                :options="sigunguSelectList"
                optionLabel="label"
                optionValue="value"
                placeholder="시/군/구 선택"
                emptyMessage="시/도를 선택해주세요"
                showClear
                class="w-1/2"
                size="small"
              />
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 버튼 그룹 -->
    <div class="flex flex-wrap items-center gap-2 justify-end">
      <Button
        label="초기화"
        icon="pi pi-replay"
        size="small"
        severity="secondary"
        @click="handleReset"
      />
      <Button
        :label="showAdvancedSearch ? '고급검색 닫기' : '고급검색'"
        icon="pi pi-filter"
        size="small"
        variant="outlined"
        @click="showAdvancedSearch = !showAdvancedSearch"
      />
    </div>
  </div>
</template>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.1s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.fade-slide-enter-to,
.fade-slide-leave-from {
  opacity: 1;
  transform: translateY(0);
}
</style>
