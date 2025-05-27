import { createApp } from 'vue'
import { pinia } from './stores'

import PrimeVue from 'primevue/config'

import './assets/main.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(PrimeVue, {
  theme: 'none',
  unstyled: false,
  pt: {
    // 다크모드 비활성화를 위한 설정
    global: {
      dark: false,
    },
  },
  locale: {
    dayNames: ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일'],
    dayNamesShort: ['일', '월', '화', '수', '목', '금', '토'],
    dayNamesMin: ['일', '월', '화', '수', '목', '금', '토'],
    monthNames: [
      '1월',
      '2월',
      '3월',
      '4월',
      '5월',
      '6월',
      '7월',
      '8월',
      '9월',
      '10월',
      '11월',
      '12월',
    ],
    monthNamesShort: [
      '1월',
      '2월',
      '3월',
      '4월',
      '5월',
      '6월',
      '7월',
      '8월',
      '9월',
      '10월',
      '11월',
      '12월',
    ],
  },
})

// 공통 속성 정의
app.config.globalProperties.$appName = 'http://localhost:8080/api'
app.config.globalProperties.$appVersion = 'http://localhost:8080/api'

app.mount('#app')
