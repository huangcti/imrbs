import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/Home.vue')
    },
    {
      path: '/rooms/search',
      name: 'room-search',
      component: () => import('../views/RoomSearch.vue')
    }
  ]
})

export default router
