import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'

export const useBidStore = defineStore('bidSearch', () => {
  const bids = ref([])
  const bid = ref(null)
  const awardsRaw = ref([]) // 원본 낙찰 공고
  const awards = computed(() =>
    awardsRaw.value.map((item) => ({
      ...item,
      bizs_parsed: parseBizsParsed(item.bizs_parsed),
    })),
  )
  const recommendBids = ref([])
  const bidCounts = ref([])
  const loading = ref(false)
  const error = ref(null)

  const fetchBids = async (params) => {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/bids/search', { params })
      bids.value = response.data
    } catch (err) {
      console.error('입찰 검색 실패:', err)
      error.value = err
    } finally {
      loading.value = false
    }
  }

  const fetchBid = async (bidId) => {
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/bids/${bidId}`)
      bid.value = response.data?.currentBids || null
      awardsRaw.value = response.data?.pastData || []
      console.log('🐛 공고 조회 : ', response.data)
    } catch (err) {
      console.error('공고 상세 조회 실패:', err)
      alert('공고 조회 실패하였습니다.')
      error.value = err
    } finally {
      loading.value = false
    }
  }

  const fetchRecommendedBid = async () => {
    try {
      const res = await api.get('/recommend')
      recommendBids.value = res.data
      console.log('🐛  추천 공고 조회 : ', res.data)
    } catch (error) {
      console.error('추천 입찰 공고 불러오기 실패:', error)
    }
  }

  const clear = () => {
    bids.value = []
    error.value = null
  }

  // 낙찰 공고 파싱
  const parseBizsParsed = (rawList) => {
    if (!Array.isArray(rawList)) return []

    return rawList
      .filter((row) => Array.isArray(row) && row.length >= 6)
      .map((row) => ({
        opengRank: row[0] || '-', // 순위
        prcbdrBizno: row[1] || '', // 사업자번호
        prcbdrNm: row[2] || '', // 기업명
        bidprcAmt: row[3] ? Number(row[3]) : null, // 입찰 금액 (빈 문자열이면 null)
        bidprcrt: row[4] ? Number(row[4]) : null, // 투찰율 (빈 문자열이면 null)
        rmrk: row[5] || '', // 비고
        emplyeNum: row[6] ? Number(row[6]) : null, // 종업원 수
        indstrytyCd: row[7] || null, // 업종코드
        presmptPrce: row[8] ? Number(row[8]) : null, // 최근 참여 금액
      }))
      .sort((a, b) => {
        const rankA = parseInt(a.opengRank, 10)
        const rankB = parseInt(b.opengRank, 10)

        if (isNaN(rankA)) return 1
        if (isNaN(rankB)) return -1
        return rankA - rankB
      })
      .slice(0, 5)
  }

  const fetchBidCount = async () => {
    try {
      const res = await api.get('/bids/major-category/count')
      bidCounts.value = res.data
      // console.log('🐛  공고 집계 : ', res.data)
    } catch (error) {
      console.error('공고 집계 실패:', error)
    }
  }

  return {
    bids,
    bid,
    awards,
    recommendBids,
    bidCounts,
    loading,
    error,
    fetchBids,
    fetchBid,
    fetchRecommendedBid,
    fetchBidCount,
    clear,
  }
})
