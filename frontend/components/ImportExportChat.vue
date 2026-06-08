<script setup lang="ts">
import { useMainStore } from '~/store'
import { useUiText } from '~/composables/useUiText'

const props = defineProps(['allowDownloadAll'])
const store = useMainStore()
const { t } = useUiText()
const showModal = ref(false)
const chatImportRef = ref<HTMLInputElement | null>(null)
const errorMessage = ref<string | undefined>(undefined)
const disableUpload = ref(true)

const importChatPath = computed(() =>
  useRuntimeConfig().public.api.importChatById.replace(':chatId', store.chatActive?.chatId?.toString())
)

const linkDownload = computed(() =>
  props.allowDownloadAll
    ? useRuntimeConfig().public.api.exportAllChats
    : useRuntimeConfig().public.api.exportChatById.replace(':chatId', store.chatActive?.chatId?.toString())
)

const chatName = computed(() =>
  props.allowDownloadAll ? 'all-chats.zip' : store.chatActive.chatName + '.zip'
)

function toggleModal() {
  showModal.value = !showModal.value
  errorMessage.value = undefined
  if (chatImportRef.value) chatImportRef.value.value = ''
}

function onFilePicked() {
  disableUpload.value = !(chatImportRef.value?.files?.[0])
}

async function uploadFile() {
  if (!chatImportRef.value?.files?.[0]) return
  store.loading = true
  const form = new FormData()
  form.append('file', chatImportRef.value.files[0])
  $fetch(importChatPath.value, { method: 'POST', body: form })
    .then(() => {
      store.loading = false
      if (chatImportRef.value) chatImportRef.value.value = ''
      store.clearMessages()
    })
    .catch((e) => {
      store.loading = false
      errorMessage.value = e.data?.detail
    })
}

watch(() => store.chatActive.chatId, () => {
  disableUpload.value = true
  if (chatImportRef.value) chatImportRef.value.value = ''
})
</script>

<template>
  <div>
    <button class="wa-btn-ghost text-sm py-2.5 w-full" @click="toggleModal">
      {{ t('importExport') }}
    </button>

    <!-- Modal -->
    <teleport to="body">
      <div
        v-if="showModal"
        class="fixed inset-0 z-[1050] flex items-center justify-center bg-black/70 backdrop-blur-sm"
        @click.self="toggleModal"
      >
        <div class="bg-wa-header rounded-2xl shadow-2xl w-full max-w-md mx-4 border border-wa-border overflow-hidden">
          <!-- Header -->
          <div class="flex items-center justify-between px-5 py-4 border-b border-wa-border">
            <h2 class="text-wa-text font-semibold text-base">{{ t('importExportTitle') }}</h2>
            <button
              class="p-1.5 rounded-full text-wa-icon hover:bg-wa-hover transition-colors"
              :aria-label="t('close')"
              @click="toggleModal"
            >
              <svg viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
                <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
              </svg>
            </button>
          </div>

          <!-- Body -->
          <div class="px-5 py-4 flex flex-col gap-4">
            <div v-if="errorMessage" class="bg-red-900/30 border border-red-700/40 text-red-300 rounded-lg px-4 py-3 text-sm">
              {{ t('failedToImport') }}: {{ errorMessage }}
            </div>

            <!-- Export -->
            <div>
              <p class="text-wa-text-muted text-xs uppercase tracking-wider mb-2">{{ t('getEntireChat') }}</p>
              <a class="wa-btn flex items-center justify-center text-sm w-full" :href="linkDownload" :download="chatName">
                Baixar {{ chatName }}
              </a>
            </div>

            <!-- Import (only for existing chat) -->
            <template v-if="!allowDownloadAll">
              <div class="border-t border-wa-border pt-4">
                <p class="text-wa-text-muted text-xs mb-2">{{ t('importMessagesToChat') }}</p>
                <input
                  class="block w-full text-sm text-wa-text-muted mb-3
                         file:mr-3 file:py-1.5 file:px-3 file:rounded-lg file:border-0
                         file:text-sm file:font-medium file:bg-wa-hover file:text-wa-text
                         hover:file:bg-wa-hover/80 cursor-pointer"
                  type="file" accept=".zip,.txt"
                  ref="chatImportRef"
                  @change="onFilePicked"
                />
                <button
                  class="wa-btn w-full text-sm"
                  :disabled="disableUpload"
                  @click="uploadFile"
                >
                  {{ t('upload') }}
                </button>
              </div>
            </template>
          </div>

          <!-- Footer -->
          <div class="px-5 pb-5">
            <button class="wa-btn-ghost w-full text-sm" @click="toggleModal">{{ t('close') }}</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>
