<script setup lang="ts">
import { useMainStore } from '~/store'
import { useUiText } from '~/composables/useUiText'

const store = useMainStore()
const { t } = useUiText()
const showModal = ref(false)
const imageImportRef = ref<HTMLInputElement | null>(null)
const imageUrl = ref<string | ArrayBuffer | null>(null)
const fileValid = ref(false)

const profileImagePath = computed(() =>
  useRuntimeConfig().public.api.getProfileImage.replace(':chatId', store.chatActive.chatId.toString())
)

function resetFileInput() {
  if (imageImportRef.value) imageImportRef.value.value = ''
  fileValid.value = false
  imageUrl.value = null
}

function toggleModal() {
  showModal.value = !showModal.value
  if (!showModal.value) resetFileInput()
}

function previewFile(file: File) {
  const reader = new FileReader()
  reader.onload = () => { imageUrl.value = reader.result }
  reader.readAsDataURL(file)
}

function onFilePicked(event: Event) {
  const file = (event.target as HTMLInputElement)?.files?.[0]
  if (file) { fileValid.value = true; previewFile(file) }
}

async function uploadFile() {
  if (!imageImportRef.value?.files?.[0]) return
  const formData = new FormData()
  formData.append('profile-image', imageImportRef.value.files[0])
  try {
    await $fetch(profileImagePath.value, { method: 'POST', body: formData })
    store.reloadImageProfile = true
    toggleModal()
  } catch (e) {
    console.error('Error uploading file:', e)
  } finally {
    resetFileInput()
  }
}
</script>

<template>
  <div>
    <!-- Trigger: profile image with pencil overlay -->
    <button
      class="relative group rounded-full focus:outline-none"
      :aria-label="t('changeAvatarAria')"
      @click="toggleModal"
    >
      <profile-image :id="store.chatActive.chatId" :cache-url="true" class="w-16 h-16" />
      <div class="absolute inset-0 rounded-full bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
        <icon-pencil-square class="w-5 h-5 text-white" />
      </div>
    </button>

    <!-- Modal -->
    <teleport to="body">
      <div
        v-if="showModal"
        class="fixed inset-0 z-[1050] flex items-center justify-center bg-black/70 backdrop-blur-sm"
        @click.self="toggleModal"
      >
        <div class="bg-wa-header rounded-2xl shadow-2xl w-full max-w-sm mx-4 border border-wa-border overflow-hidden">
          <!-- Header -->
          <div class="flex items-center justify-between px-5 py-4 border-b border-wa-border">
            <h2 class="text-wa-text font-semibold text-base">{{ t('updateAvatarTitle') }}</h2>
            <button class="p-1.5 rounded-full text-wa-icon hover:bg-wa-hover" @click="toggleModal">
              <svg viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
                <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
              </svg>
            </button>
          </div>

          <!-- Body -->
          <div class="px-5 py-4 flex flex-col gap-4">
            <!-- Preview -->
            <div class="flex justify-center">
              <div class="w-24 h-24 rounded-full overflow-hidden border-2 border-wa-border">
                <profile-image
                  :id="store.chatActive.chatId"
                  :cache-url="false"
                  :url-provided="imageUrl"
                  class="w-full h-full"
                />
              </div>
            </div>

            <!-- File input -->
            <div>
              <input
                class="block w-full text-sm text-wa-text-muted
                       file:mr-3 file:py-1.5 file:px-3 file:rounded-lg file:border-0
                       file:text-sm file:font-medium file:bg-wa-hover file:text-wa-text
                       hover:file:bg-wa-hover/80 cursor-pointer"
                type="file"
                accept=".jpg"
                ref="imageImportRef"
                :aria-label="t('selectProfileImageAria')"
                @change="onFilePicked"
              />
              <p class="text-wa-text-muted text-xs mt-1.5">{{ t('jpgOnlyHint') }}</p>
            </div>
          </div>

          <!-- Footer -->
          <div class="flex gap-3 px-5 pb-5">
            <button class="wa-btn-ghost flex-1 text-sm" @click="toggleModal">{{ t('cancel') }}</button>
            <button class="wa-btn flex-1 text-sm" :disabled="!fileValid" @click="uploadFile">
              {{ t('saveChanges') }}
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>
