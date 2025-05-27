<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()

const company = reactive({
  businessNumber: '',
  corpName: '',
  openDate: '',
  ceoName: '',
  emplyeNum: '',
})

const errors = ref({
  businessNumber: '',
  corpName: '',
  openDate: '',
  ceoName: '',
  emplyeNum: '',
})

const goToLogin = () => {
  router.push('/login')
}

// 사업자등록번호 확인 API 호출
const checkBusinessNumber = async () => {
  const bizno = company.businessNumber.trim()

  if (!/^\d{10}$/.test(bizno)) {
    errors.value.businessNumber = '올바른 사업자등록번호를 입력해주세요 (10자리 숫자)'
    return
  }

  errors.value.businessNumber = ''

  try {
    const { data } = await axios.get(
      'https://apis.data.go.kr/1230000/ao/UsrInfoService/getPrcrmntCorpBasicInfo',
      {
        params: {
          serviceKey: import.meta.env.VITE_PUBLIC_DATA_SERVICE_KEY,
          pageNo: '1',
          numOfRows: '1',
          inqryDiv: '3',
          type: 'json',
          bizno: bizno,
        },
      },
    )
    const result = data?.response?.body?.items
    if (!result) {
      errors.value.businessNumber = '기업 정보를 찾을 수 없습니다'
      return
    }

    console.log('🐛 기업정보 : ', data)

    const { data: indstryData } = await axios.get(
      'https://apis.data.go.kr/1230000/ao/UsrInfoService/getPrcrmntCorpIndstrytyInfo',
      {
        params: {
          serviceKey: import.meta.env.VITE_PUBLIC_DATA_SERVICE_KEY,
          pageNo: '1',
          numOfRows: '1',
          type: 'json',
          inqryDiv: '1',
          bizno: bizno,
        },
      },
    )

    const industryResult = indstryData?.response?.body?.items
    const industry = Array.isArray(industryResult) ? industryResult[0] : industryResult
    const industryCode = industry?.indstrytyCd || ''

    console.log('🐛 산업정보:', indstryData)

    // 여러 형태로 응답이 올 수 있으니 배열/객체 체크
    const corp = Array.isArray(result) ? result[0] : result

    company.corpName = corp?.corpNm || ''
    company.openDate = (corp?.opbizDt || '').split(' ')[0]
    company.ceoName = corp?.ceoNm || ''
    company.emplyeNum = corp?.emplyeNum || ''
    company.industryCode = industryCode
  } catch (err) {
    console.error('사업자조회 오류:', err)
    errors.value.businessNumber = '기업 정보를 조회하는 중 오류가 발생했습니다'
  }
}

const handleSubmit = async () => {
  // 필수값 검증
  if (!company.businessNumber || !/^\d{10}$/.test(company.businessNumber)) {
    errors.value.businessNumber = '올바른 사업자등록번호를 입력해주세요 (10자리 숫자)'
    return
  }

  try {
    await userStore.registerCorp({
      businessNumber: company.businessNumber,
      corpName: company.corpName,
      openDate: company.openDate,
      ceoName: company.ceoName,
      emplyeNum: company.emplyeNum,
      IndustryCode: company.industryCode,
    })

    alert('기업정보 등록이 완료되었습니다.')
    router.push('/')
  } catch (error) {
    console.error('기업정보 등록 오류:', error)
    alert('기업정보 등록 중 오류가 발생했습니다.')
  }
}
</script>

<template>
  <div class="min-h-[calc(100vh-160px)] flex justify-center items-center px-5 py-10 bg-gray-100">
    <div class="w-full max-w-[1000px] flex flex-col items-center gap-8">
      <!-- 로고 및 타이틀 -->
      <div class="text-center">
        <img src="@/assets/logo.png" alt="Logo" class="h-16 mb-4 mx-auto" />
        <h1 class="text-3xl font-bold text-blue-500">나라장터 입찰정보 플랫폼</h1>
        <p class="text-lg text-gray-600 mt-2">기업정보를 등록하여 서비스를 이용하세요</p>
      </div>

      <!-- 기업정보 등록 카드 -->
      <Card class="w-full max-w-3xl p-4">
        <template #title>
          <div class="mb-4">
            <h2 class="text-2xl font-semibold text-gray-800">기업 정보</h2>
            <p class="text-sm text-gray-500 mt-1">
              사업자등록번호로 나라장터에 등록된 기업정보를 조회합니다
            </p>
          </div>
        </template>

        <template #content>
          <form @submit.prevent="handleSubmit" class="space-y-5">
            <!-- 사업자등록번호 + 확인버튼 한 줄 정렬 -->
            <div>
              <label for="businessNumber" class="block text-sm font-medium text-gray-700 mb-1"
                >사업자등록번호</label
              >
              <div class="flex gap-2">
                <InputText
                  id="businessNumber"
                  v-model="company.businessNumber"
                  class="flex-1"
                  placeholder="사업자등록번호"
                />
                <Button
                  label="확인"
                  @click="checkBusinessNumber"
                  class="p-button-outlined"
                  size="small"
                />
              </div>
              <small class="text-red-500">{{ errors.businessNumber }}</small>
            </div>

            <!-- 업체명 -->
            <div>
              <label for="corpName" class="block text-sm font-medium text-gray-700 mb-1"
                >업체명</label
              >
              <InputText
                id="corpName"
                v-model="company.corpName"
                class="w-full"
                placeholder="업체명"
                disabled
              />
              <small class="text-red-500">{{ errors.corpName }}</small>
            </div>

            <!-- 개업 일자 -->
            <div>
              <label for="openDate" class="block text-sm font-medium text-gray-700 mb-1"
                >개업 일자</label
              >
              <InputText
                id="openDate"
                v-model="company.openDate"
                class="w-full"
                placeholder="개업 일자"
                disabled
              />
              <small class="text-red-500">{{ errors.openDate }}</small>
            </div>

            <!-- 대표자명 -->
            <div>
              <label for="ceoName" class="block text-sm font-medium text-gray-700 mb-1"
                >대표자명</label
              >
              <InputText
                id="ceoName"
                v-model="company.ceoName"
                class="w-full"
                placeholder="대표자명"
                disabled
              />
              <small class="text-red-500">{{ errors.ceoName }}</small>
            </div>

            <!-- 종업원 수 -->
            <div>
              <label for="emplyeNum" class="block text-sm font-medium text-gray-700 mb-1"
                >종업원 수</label
              >
              <InputText
                id="emplyeNum"
                v-model="company.emplyeNum"
                class="w-full"
                placeholder="종업원 수"
                disabled
              />
              <small class="text-red-500">{{ errors.emplyeNum }}</small>
            </div>

            <!-- 버튼 영역 -->
            <div class="flex justify-between mt-6">
              <Button
                label="취소"
                @click="goToLogin"
                class="p-button-outlined"
                severity="secondary"
                size="small"
              />
              <Button label="등록" @click="handleSubmit" class="p-button-primary" size="small" />
            </div>
          </form>
        </template>
      </Card>
    </div>
  </div>
</template>

<style scoped></style>
