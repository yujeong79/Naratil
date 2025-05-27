<script setup>
import { computed } from 'vue'

const props = defineProps({
  notice: {
    type: Object,
    required: true,
  },
})

const now = new Date()
const toDate = (d) => (d ? new Date(d) : null)

const steps = computed(() => {
  if (!props.notice) return []

  const bidNtceDt = toDate(props.notice.bidNtceDt)
  const bidBeginDt = toDate(props.notice.bidBeginDt)
  const bidClseDt = toDate(props.notice.bidClseDt)
  const opengDt = toDate(props.notice.opengDt)

  const imminentThreshold = bidClseDt
    ? new Date(bidClseDt.getTime() - 1 * 24 * 60 * 60 * 1000)
    : null

  return [
    {
      status: '입찰 전',
      date: bidNtceDt,
      condition: bidBeginDt && now < bidBeginDt,
    },
    {
      status: '입찰 개시',
      date: bidBeginDt,
      condition: bidBeginDt && bidClseDt && now >= bidBeginDt && now < imminentThreshold,
    },
    {
      status: '마감 임박',
      date: imminentThreshold,
      condition: imminentThreshold && bidClseDt && now >= imminentThreshold && now < bidClseDt,
    },
    {
      status: '입찰 마감',
      date: bidClseDt,
      condition: bidClseDt && opengDt && now >= bidClseDt && now < opengDt,
    },
    {
      status: '개찰',
      date: opengDt,
      condition: opengDt && now >= opengDt,
    },
  ]
})

const activeStep = computed(() => steps.value.findIndex((s) => s.condition) ?? 0)

const formatTime = (date) => {
  if (!date) return '-'
  return new Intl.DateTimeFormat('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date)
}

const formatDate = (date) => {
  if (!date) return '-'
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}
</script>

<template>
  <div v-if="notice">
    <h3 class="text-xl font-bold text-gray-700 mb-6">입찰 진행 상태</h3>

    <div class="flex items-start justify-between relative">
      <div class="w-1/2 h-0.5 bg-gray-300 absolute left-0 top-[28px] z-0"></div>

      <div
        v-for="(step, index) in steps"
        :key="index"
        class="flex-1 relative flex flex-col items-center text-center"
      >
        <div
          v-if="index !== steps.length - 1"
          class="absolute top-[28px] left-1/2 w-full h-0.5 bg-gray-300 z-0"
        ></div>

        <div
          class="z-10 w-3 h-3 rounded-full absolute top-[28px] -translate-y-1/2"
          :class="{
            'bg-blue-600': index === activeStep,
            'bg-gray-300': index !== activeStep,
          }"
        ></div>

        <div
          class="mt-10 text-sm text-gray-800"
          :class="{ 'font-bold': index === activeStep, 'font-medium': index !== activeStep }"
        >
          {{ step.status }}
        </div>
        <div
          class="text-xs text-gray-500 leading-tight"
          :class="{ 'font-bold': index === activeStep, 'font-medium': index !== activeStep }"
        >
          {{ formatDate(step.date) }}<br />
          {{ formatTime(step.date) }}
        </div>
      </div>

      <div class="w-1/2 h-0.5 bg-gray-300 absolute right-0 top-[28px] z-0"></div>
    </div>
  </div>
</template>
<style scoped></style>
