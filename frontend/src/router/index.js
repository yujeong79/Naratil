import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Main',
      component: () => import('@/views/MainView.vue'),
      meta: {
        title: '메인',
      },
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: {
        title: '로그인',
      },
    },
    {
      path: '/signup',
      name: 'Signup',
      component: () => import('@/views/SignupView.vue'),
      meta: {
        title: '회원가입',
      },
    },
    {
      path: '/register-corp',
      name: 'RegisterCorp',
      component: () => import('@/views/RegisterCorpView.vue'),
      meta: {
        title: '기업정보 등록',
      },
    },
    {
      path: '/bids',
      name: 'Bids',
      component: () => import('@/views/BidListView.vue'),
      meta: {
        title: '입찰공고목록',
      },
    },
    {
      path: '/bids/:bidNtceId',
      name: 'BidDetail',
      component: () => import('@/views/BidDetailView.vue'),
      props: true,
      meta: {
        title: '입찰공고 상세',
      },
    },
    {
      path: '/mypage',
      name: 'Mypage',
      component: () => import('@/views/MyPageView.vue'),
      meta: {
        title: '마이페이지',
      },
    },
  ],
})

export default router
