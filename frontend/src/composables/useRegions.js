import { ref, computed, onMounted } from 'vue'

export function useRegions() {
  const regions = ref([])
  const selectedSido = ref(null)
  const selectedSigungu = ref(null)

  const sigunguList = computed(() => {
    const region = regions.value.find((r) => r.sido === selectedSido.value)
    return region
      ? region.sigungu.map((s) => ({
          label: s.name,
          value: s.name,
        }))
      : []
  })

  const fetchRegions = async () => {
    try {
      const res = await fetch('/data/regions.json')
      regions.value = await res.json()
    } catch (err) {
      console.error('지역 정보를 불러오는 데 실패했습니다:', err)
    }
  }

  onMounted(fetchRegions)

  return {
    regions,
    selectedSido,
    selectedSigungu,
    sigunguList,
  }
}
