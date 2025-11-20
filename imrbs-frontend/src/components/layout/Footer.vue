<!--
T048 [P] 建立通用佈局元件 - Footer
頁尾,包含版權資訊、快速連結
-->

<script setup lang="ts">
const currentYear = new Date().getFullYear()
</script>

<template>
  <footer class="bg-gray-50 border-t border-gray-200 mt-auto">
    <div class="container mx-auto px-4 py-8">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <!-- 關於系統 -->
        <div>
          <h3 class="text-lg font-semibold text-gray-800 mb-4">
            IMRBS 會議室預約系統
          </h3>
          <p class="text-sm text-gray-600">
            提供便捷的會議室預約、查詢與管理服務,讓團隊協作更有效率。
          </p>
        </div>

        <!-- 快速連結 -->
        <div>
          <h3 class="text-lg font-semibold text-gray-800 mb-4">
            快速連結
          </h3>
          <ul class="space-y-2">
            <li>
              <router-link
                to="/"
                class="text-sm text-gray-600 hover:text-blue-600 transition-colors"
              >
                首頁
              </router-link>
            </li>
            <li>
              <router-link
                to="/rooms"
                class="text-sm text-gray-600 hover:text-blue-600 transition-colors"
              >
                查詢會議室
              </router-link>
            </li>
            <li>
              <router-link
                to="/reservations"
                class="text-sm text-gray-600 hover:text-blue-600 transition-colors"
              >
                我的預約
              </router-link>
            </li>
            <li>
              <router-link
                to="/help"
                class="text-sm text-gray-600 hover:text-blue-600 transition-colors"
              >
                使用說明
              </router-link>
            </li>
          </ul>
        </div>

        <!-- 聯絡資訊 -->
        <div>
          <h3 class="text-lg font-semibold text-gray-800 mb-4">
            聯絡我們
          </h3>
          <ul class="space-y-2 text-sm text-gray-600">
            <li class="flex items-center space-x-2">
              <i class="pi pi-envelope" />
              <span>support@company.com</span>
            </li>
            <li class="flex items-center space-x-2">
              <i class="pi pi-phone" />
              <span>+886-2-1234-5678</span>
            </li>
            <li class="flex items-center space-x-2">
              <i class="pi pi-map-marker" />
              <span>台北市信義區某某路 123 號</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- 版權資訊 -->
      <div class="border-t border-gray-200 mt-8 pt-6 text-center">
        <p class="text-sm text-gray-500">
          © {{ currentYear }} IMRBS. All rights reserved.
        </p>
      </div>
    </div>
  </footer>
</template>
