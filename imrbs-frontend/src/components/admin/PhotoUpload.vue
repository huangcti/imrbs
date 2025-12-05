<template>
  <div class="space-y-4">
    <!-- 現有照片顯示 -->
    <div v-if="photos.length > 0" class="space-y-2">
      <h3 class="text-sm font-medium text-gray-700">現有照片</h3>
      <div class="grid grid-cols-3 gap-4">
        <div
          v-for="(photo, index) in photos"
          :key="photo"
          class="relative group"
          :data-testid="`photo-item-${index}`"
        >
          <img
            :src="photo"
            :alt="`會議室照片 ${index + 1}`"
            class="w-full h-32 object-cover rounded-md border border-gray-300"
          />
          <button
            type="button"
            @click="removePhoto(index)"
            :data-testid="`remove-photo-${index}`"
            class="absolute top-2 right-2 p-1 bg-red-600 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-700"
            title="刪除照片"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 上傳區域 -->
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-2">
        上傳照片
      </label>
      <div
        @dragover.prevent="isDragging = true"
        @dragleave.prevent="isDragging = false"
        @drop.prevent="handleDrop"
        :class="[
          'border-2 border-dashed rounded-md p-6 text-center cursor-pointer transition-colors',
          isDragging ? 'border-blue-500 bg-blue-50' : 'border-gray-300 hover:border-gray-400'
        ]"
        data-testid="upload-dropzone"
      >
        <input
          ref="fileInput"
          type="file"
          accept="image/jpeg,image/jpg,image/png,image/webp"
          multiple
          @change="handleFileSelect"
          data-testid="file-input"
          class="hidden"
        />

        <div @click="triggerFileInput" class="space-y-2">
          <svg
            class="mx-auto h-12 w-12 text-gray-400"
            stroke="currentColor"
            fill="none"
            viewBox="0 0 48 48"
          >
            <path
              d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <div class="text-sm text-gray-600">
            <span class="font-medium text-blue-600 hover:text-blue-500">點擊上傳</span>
            或拖曳檔案至此
          </div>
          <p class="text-xs text-gray-500">
            支援 JPEG, PNG, WebP (最大 5MB)
          </p>
        </div>
      </div>
    </div>

    <!-- 上傳進度 -->
    <div v-if="uploadingFiles.length > 0" class="space-y-2">
      <h3 class="text-sm font-medium text-gray-700">上傳中...</h3>
      <div
        v-for="file in uploadingFiles"
        :key="file.name"
        class="flex items-center gap-3 p-3 bg-gray-50 rounded-md"
      >
        <div class="flex-1">
          <p class="text-sm font-medium text-gray-700">{{ file.name }}</p>
          <div class="mt-1 w-full bg-gray-200 rounded-full h-2">
            <div
              class="bg-blue-600 h-2 rounded-full transition-all duration-300"
              :style="{ width: `${file.progress}%` }"
            ></div>
          </div>
        </div>
        <span class="text-sm text-gray-500">{{ file.progress }}%</span>
      </div>
    </div>

    <!-- 錯誤訊息 -->
    <div v-if="errorMessage" class="p-3 bg-red-50 border border-red-200 rounded-md">
      <p class="text-sm text-red-600" data-testid="error-message">{{ errorMessage }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'

// T124 [P] [US4] 照片上傳元件

interface Props {
  roomId: number
  photos: string[]
}

interface Emits {
  (event: 'update:photos', photos: string[]): void
  (event: 'upload-success', photoUrl: string): void
  (event: 'upload-error', error: string): void
}

interface UploadingFile {
  name: string
  progress: number
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const fileInput = ref<HTMLInputElement>()
const isDragging = ref(false)
const uploadingFiles = ref<UploadingFile[]>([])
const errorMessage = ref('')

// 觸發檔案選擇
const triggerFileInput = () => {
  fileInput.value?.click()
}

// 處理檔案選擇
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files) {
    uploadFiles(Array.from(target.files))
    target.value = '' // 清空 input
  }
}

// 處理拖曳放下
const handleDrop = (event: DragEvent) => {
  isDragging.value = false
  if (event.dataTransfer?.files) {
    uploadFiles(Array.from(event.dataTransfer.files))
  }
}

// 驗證檔案
const validateFile = (file: File): string | null => {
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
  const maxSize = 5 * 1024 * 1024 // 5MB

  if (!allowedTypes.includes(file.type)) {
    return `不支援的檔案類型: ${file.name} (僅支援 JPEG, PNG, WebP)`
  }

  if (file.size > maxSize) {
    return `檔案過大: ${file.name} (最大 5MB)`
  }

  return null
}

// 上傳檔案
const uploadFiles = async (files: File[]) => {
  errorMessage.value = ''

  for (const file of files) {
    // 驗證檔案
    const error = validateFile(file)
    if (error) {
      errorMessage.value = error
      emit('upload-error', error)
      continue
    }

    // 新增到上傳清單
    const uploadingFile: UploadingFile = { name: file.name, progress: 0 }
    uploadingFiles.value.push(uploadingFile)

    try {
      // 建立 FormData
      const formData = new FormData()
      formData.append('file', file)

      // 上傳檔案
      const response = await axios.post<{ photoUrl: string }>(
        `/api/v1/rooms/${props.roomId}/photos`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          },
          onUploadProgress: (progressEvent) => {
            if (progressEvent.total) {
              uploadingFile.progress = Math.round(
                (progressEvent.loaded * 100) / progressEvent.total
              )
            }
          }
        }
      )

      // 上傳成功
      const photoUrl = response.data.photoUrl
      emit('update:photos', [...props.photos, photoUrl])
      emit('upload-success', photoUrl)

      // 移除上傳清單
      const index = uploadingFiles.value.indexOf(uploadingFile)
      if (index > -1) {
        uploadingFiles.value.splice(index, 1)
      }
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : '上傳失敗'
      errorMessage.value = `${file.name}: ${errorMsg}`
      emit('upload-error', errorMsg)

      // 移除上傳清單
      const index = uploadingFiles.value.indexOf(uploadingFile)
      if (index > -1) {
        uploadingFiles.value.splice(index, 1)
      }
    }
  }
}

// 移除照片
const removePhoto = (index: number) => {
  const updatedPhotos = [...props.photos]
  updatedPhotos.splice(index, 1)
  emit('update:photos', updatedPhotos)
}
</script>
