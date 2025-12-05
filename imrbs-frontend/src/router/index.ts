import { createRouter, createWebHistory } from 'vue-router'
import { compositeGuard } from './guards'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ==================== 公開路由 ====================
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/auth/callback',
      name: 'auth-callback',
      component: () => import('../views/AuthCallback.vue')
    },
    {
      path: '/403',
      name: 'forbidden',
      component: () => import('../views/Forbidden.vue')
    },
    {
      path: '/guest',
      name: 'guest-request',
      component: () => import('../views/GuestRequest.vue'),
      meta: { requiresGuest: false } // 公開頁面，訪客可直接進入
    },

    // ==================== 需要認證的路由 ====================
    {
      path: '/',
      name: 'home',
      component: () => import('../views/Home.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/rooms/search',
      name: 'room-search',
      component: () => import('../views/RoomSearch.vue'),
      meta: { 
        requiresAuth: true,
        requiresAnyRole: ['EMPLOYEE', 'ROOM_ADMIN', 'SYSTEM_ADMIN']
      }
    },
    {
      path: '/reservations/my',
      name: 'my-reservations',
      component: () => import('../views/MyReservations.vue'),
      meta: { 
        requiresAuth: true,
        requiresAnyRole: ['EMPLOYEE', 'ROOM_ADMIN', 'SYSTEM_ADMIN']
      }
    },

    // ==================== 管理員路由 ====================
    {
      path: '/admin/guest-approval',
      name: 'admin-guest-approval',
      component: () => import('../views/admin/GuestApproval.vue'),
      meta: { 
        requiresAuth: true,
        requiresAnyRole: ['ROOM_ADMIN', 'SYSTEM_ADMIN']
      }
    },
    // TODO: US4 - 會議室管理功能實作後啟用
    // {
    //   path: '/admin/rooms',
    //   name: 'admin-rooms',
    //   component: () => import('../views/admin/RoomManagement.vue'),
    //   meta: { 
    //     requiresAuth: true,
    //     requiresAnyRole: ['ROOM_ADMIN', 'SYSTEM_ADMIN']
    //   }
    // },
    // {
    //   path: '/admin/users',
    //   name: 'admin-users',
    //   component: () => import('../views/admin/UserManagement.vue'),
    //   meta: { 
    //     requiresAuth: true,
    //     requiresRole: 'SYSTEM_ADMIN'
    //   }
    // }
  ]
})

// 註冊全域路由守衛
router.beforeEach(compositeGuard)

export default router
