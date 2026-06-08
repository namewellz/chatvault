<script setup lang="ts">
import { useMainStore } from '~/store'
import { useUiText } from '~/composables/useUiText'

const props = defineProps(['allowDownloadAll'])
const store = useMainStore()
const { t } = useUiText()
const emit = defineEmits(['exit:dialog'])

const linkDownload = computed(() =>
  props.allowDownloadAll
    ? useRuntimeConfig().public.api.exportAllChats
    : useRuntimeConfig().public.api.exportChatById.replace(':chatId', store.chatActive?.chatId?.toString())
)

const chatName = computed(() =>
  props.allowDownloadAll ? 'all-chats.zip' : store.chatActive.chatName + '.zip'
)

function cancel() { emit('exit:dialog') }
</script>

<template>
  <div class="absolute inset-0 z-40 flex items-center justify-center bg-black/60 backdrop-blur-sm">
    <div class="bg-wa-header rounded-2xl shadow-2xl w-full max-w-sm mx-4 overflow-hidden border border-wa-border">
      <!-- Header -->
      <div class="flex items-center justify-between px-5 py-4 border-b border-wa-border">
        <h2 class="text-wa-text font-semibold text-base">{{ t('executeChatExport') }}</h2>
        <button class="p-1.5 rounded-full text-wa-icon hover:bg-wa-hover transition-colors" @click="cancel">
          <svg viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
          </svg>
        </button>
      </div>

      <!-- Body -->
      <div class="px-5 py-6 flex flex-col items-center gap-4">
        <svg viewBox="0 0 24 24" class="w-12 h-12 text-wa-green" fill="currentColor">
          <path d="M19 9h-4V3H9v6H5l7 7 7-7zm-8 2V5h2v6h1.17L12 13.17 9.83 11H11zm-6 7h14v2H5v-2z"/>
        </svg>
        <p class="text-wa-text text-center text-sm">
          {{ t('getEntireChat') }}: <span class="text-wa-green font-medium">{{ chatName }}</span>
        </p>
        <a
          class="wa-btn text-center w-full text-sm"
          :href="linkDownload"
          :download="chatName"
        >
          Baixar arquivo
        </a>
      </div>

      <div class="px-5 pb-5">
        <button class="wa-btn-ghost w-full text-sm" @click="cancel">{{ t('cancel') }}</button>
      </div>
    </div>
  </div>
</template>
