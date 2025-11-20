<!--
主佈局元件 - 包含 Header, Footer 和內容區
-->

<script setup lang="ts">
import Header from './Header.vue'
import Footer from './Footer.vue'
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <Header />
    <main class="flex-1 container mx-auto px-4 py-8">
      <slot />
    </main>
    <Footer />
  </div>
</template>
