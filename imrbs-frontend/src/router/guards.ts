/**
 * T107 [P] [US3] 實作路由守衛 (登入檢查)
 * 保護需要認證的路由,檢查使用者權限
 * 
 * 守衛類型:
 * - requiresAuth: 需要登入
 * - requiresRole: 需要特定角色
 * - requiresAnyRole: 需要任一角色
 * - requiresGuest: 僅未登入使用者可訪問
 * 
 * @author IMRBS Team
 * @since 2025-11-24
 */

import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { UserRole } from '@/types/auth'

/**
 * 認證守衛 - 檢查使用者是否已登入
 * 
 * 用法:
 * ```typescript
 * {
 *   path: '/dashboard',
 *   component: Dashboard,
 *   meta: { requiresAuth: true }
 * }
 * ```
 */
export function authGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
): void {
  const authStore = useAuthStore()

  // 檢查路由是否需要認證
  if (to.meta.requiresAuth) {
    if (authStore.isAuthenticated) {
      next()
    } else {
      // 未登入,重定向到登入頁,並保存原始目標路徑
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    }
  } else {
    next()
  }
}

/**
 * 角色守衛 - 檢查使用者是否擁有特定角色
 * 
 * 用法:
 * ```typescript
 * {
 *   path: '/admin/rooms',
 *   component: RoomManagement,
 *   meta: { 
 *     requiresAuth: true,
 *     requiresRole: 'ROOM_ADMIN'
 *   }
 * }
 * ```
 */
export function roleGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
): void {
  const authStore = useAuthStore()

  // 檢查路由是否需要特定角色
  if (to.meta.requiresRole) {
    const requiredRole = to.meta.requiresRole as UserRole

    if (authStore.hasRole(requiredRole)) {
      next()
    } else {
      // 無權限,重定向到 403 頁面
      next({
        path: '/403',
        query: { from: to.fullPath }
      })
    }
  } else {
    next()
  }
}

/**
 * 多角色守衛 - 檢查使用者是否擁有任一角色
 * 
 * 用法:
 * ```typescript
 * {
 *   path: '/reservations',
 *   component: Reservations,
 *   meta: { 
 *     requiresAuth: true,
 *     requiresAnyRole: ['EMPLOYEE', 'ROOM_ADMIN', 'SYSTEM_ADMIN']
 *   }
 * }
 * ```
 */
export function anyRoleGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
): void {
  const authStore = useAuthStore()

  // 檢查路由是否需要任一角色
  if (to.meta.requiresAnyRole) {
    const requiredRoles = to.meta.requiresAnyRole as UserRole[]

    if (authStore.hasAnyRole(requiredRoles)) {
      next()
    } else {
      // 無權限,重定向到 403 頁面
      next({
        path: '/403',
        query: { from: to.fullPath }
      })
    }
  } else {
    next()
  }
}

/**
 * 訪客守衛 - 僅允許未登入使用者訪問
 * 
 * 用法:
 * ```typescript
 * {
 *   path: '/login',
 *   component: Login,
 *   meta: { requiresGuest: true }
 * }
 * ```
 */
export function guestGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
): void {
  const authStore = useAuthStore()

  // 檢查路由是否僅允許訪客訪問
  if (to.meta.requiresGuest) {
    if (authStore.isAuthenticated) {
      // 已登入,重定向到首頁
      next({ path: '/' })
    } else {
      next()
    }
  } else {
    next()
  }
}

/**
 * 組合守衛 - 按順序執行所有守衛
 */
export function compositeGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
): void {
  // 執行順序: 訪客守衛 -> 認證守衛 -> 角色守衛 -> 多角色守衛

  // 1. 檢查訊客限制
  if (to.meta.requiresGuest) {
    return guestGuard(to, from, next)
  }

  // 2. 檢查認證
  if (to.meta.requiresAuth) {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      return next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    }
  }

  // 3. 檢查特定角色
  if (to.meta.requiresRole) {
    const authStore = useAuthStore()
    const requiredRole = to.meta.requiresRole as UserRole

    if (!authStore.hasRole(requiredRole)) {
      return next({
        path: '/403',
        query: { from: to.fullPath }
      })
    }
  }

  // 4. 檢查任一角色
  if (to.meta.requiresAnyRole) {
    const authStore = useAuthStore()
    const requiredRoles = to.meta.requiresAnyRole as UserRole[]

    if (!authStore.hasAnyRole(requiredRoles)) {
      return next({
        path: '/403',
        query: { from: to.fullPath }
      })
    }
  }

  // 所有檢查通過
  next()
}
