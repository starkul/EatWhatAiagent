import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'
import EatAppPage from '../pages/EatAppPage.vue'
import ManusPage from '../pages/ManusPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage
    },
    {
      path: '/eat-app',
      name: 'eatApp',
      component: EatAppPage
    },
    {
      path: '/manus',
      name: 'manus',
      component: ManusPage
    }
  ]
})

export default router