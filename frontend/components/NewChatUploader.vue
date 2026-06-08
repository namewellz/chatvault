<script setup lang="ts">
import { useMainStore } from '~/store'
import { useUiText } from '~/composables/useUiText'

const store = useMainStore()
const { t } = useUiText()
const emit = defineEmits(['update:chats', 'exit:dialog'])
const chatImportRef = ref<HTMLInputElement | null>(null)
const importChatResult = ref({ data: null, errorMessage: null as string | null })
const fileValid = ref(false)
const chatName = ref<string | null>(null)

const importChatPath = computed(() => {
  if (chatName.value != null) {
    return useRuntimeConfig().public.api.importChatByName.replace(':chatName', chatName.value)
  }
})

const chatNameValid = computed(() => chatName.value !== null && chatName.value.trim() !== '')
const disableUpload = computed(() => !chatNameValid.value || !fileValid.value)

function onFilePicked() {
  fileValid.value = !!(chatImportRef.value?.files?.[0])
}

async function uploadFile() {
  if (!chatImportRef.value?.files?.[0]) return
  store.loading = true
  const form = new FormData()
  form.append('file', chatImportRef.value.files[0])
  $fetch(importChatPath.value!, { method: 'POST', body: form })
    .then(() => {
      store.loading = true
      emit('update:chats')
      if (chatImportRef.value) chatImportRef.value.value = ''
    })
    .catch((e) => {
      store.loading = false
      importChatResult.value.errorMessage = e.data?.detail ?? 'Erro desconhecido'
    })
}

function cancel() {
  emit('exit:dialog')
}
</script>

<template>
  <!-- Full-screen overlay -->
  <div class="absolute inset-0 z-40 flex items-center justify-center bg-black/60 backdrop-blur-sm">
    <div class="bg-wa-header rounded-2xl shadow-2xl w-full max-w-sm mx-4 overflow-hidden border border-wa-border">
      <!-- Header -->
      <div class="flex items-center justify-between px-5 py-4 border-b border-wa-border">
        <h2 class="text-wa-text font-semibold text-base">{{ t('createNewChat') }}</h2>
        <button class="p-1.5 rounded-full text-wa-icon hover:bg-wa-hover transition-colors" @click="cancel">
          <svg viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
          </svg>
        </button>
      </div>

      <!-- Body -->
      <div class="px-5 py-4 flex flex-col gap-4">
        <!-- Error -->
        <div v-if="importChatResult.errorMessage" class="bg-red-900/30 border border-red-700/40 text-red-300 rounded-lg px-4 py-3 text-sm">
          {{ t('failedToImport') }}: {{ importChatResult.errorMessage }}
        </div>

        <!-- Chat name -->
        <div>
          <label class="block text-wa-text-muted text-xs uppercase tracking-wider mb-1.5">{{ t('newChatNameLabel') }}</label>
          <input
            type="text"
            class="wa-input"
            v-model="chatName"
            :placeholder="t('newChatNamePlaceholder')"
          />
        </div>

        <!-- File picker -->
        <div>
          <label class="block text-wa-text-muted text-xs uppercase tracking-wider mb-1.5">{{ t('importNewChatLabel') }}</label>
          <input
            class="block w-full text-sm text-wa-text-muted
                   file:mr-3 file:py-1.5 file:px-3
                   file:rounded-lg file:border-0
                   file:text-sm file:font-medium
                   file:bg-wa-hover file:text-wa-text
                   hover:file:bg-wa-hover/80
                   cursor-pointer"
            type="file"
            accept=".zip,.txt"
            ref="chatImportRef"
            @change="onFilePicked"
          />
        </div>
      </div>

      <!-- Footer -->
      <div class="flex gap-3 px-5 pb-5">
        <button class="wa-btn flex-1" :disabled="disableUpload" @click="uploadFile">
          {{ t('uploadButton') }}
        </button>
        <button class="wa-btn-ghost flex-1" @click="cancel">
          {{ t('cancelButton') }}
        </button>
      </div>
    </div>
  </div>
</template>
