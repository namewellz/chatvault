<script setup lang="ts">
import { useMainStore } from '~/store'
import { useUiText } from '~/composables/useUiText'

const emit = defineEmits(['refresh:page'])
const store = useMainStore()
const { t } = useUiText()
const showModal = ref(false)

function toggleModal() { showModal.value = !showModal.value }

async function deleteChat() {
  const path = useRuntimeConfig().public.api.deleteChatById.replace(':chatId', store.chatActive.chatId.toString())
  await $fetch(path, { method: 'DELETE' })
  emit('refresh:page')
  store.chatExited()
}
</script>

<template>
  <div>
    <button
      class="flex items-center gap-2 px-3 py-1.5 rounded-lg border border-red-700/40 text-red-400 hover:bg-red-900/20 transition-colors text-sm"
      @click="toggleModal"
    >
      <icon-trash class="w-4 h-4" />
      {{ t('deleteChat') }}
    </button>

    <teleport to="body">
      <div
        v-if="showModal"
        class="fixed inset-0 z-[1050] flex items-center justify-center bg-black/70 backdrop-blur-sm"
        @click.self="toggleModal"
      >
        <div class="bg-wa-header rounded-2xl shadow-2xl w-full max-w-sm mx-4 border border-wa-border overflow-hidden">
          <!-- Header -->
          <div class="flex items-center justify-between px-5 py-4 border-b border-wa-border">
            <h2 class="text-wa-text font-semibold text-base">{{ t('deleteChatTitle') }}</h2>
            <button class="p-1.5 rounded-full text-wa-icon hover:bg-wa-hover" @click="toggleModal">
              <svg viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
                <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
              </svg>
            </button>
          </div>

          <!-- Body -->
          <div class="px-5 py-4">
            <p class="text-wa-text font-semibold text-sm mb-1">{{ t('deleteChatConfirmLead') }}</p>
            <p class="text-wa-text-muted text-sm mb-1">{{ t('deleteChatConfirmBody') }}</p>
            <p class="text-red-400 text-sm font-medium mb-3">{{ t('deleteChatConfirmWarning') }}</p>

            <div class="flex items-center justify-between bg-wa-hover/50 rounded-xl px-4 py-3 border border-wa-border">
              <span class="text-wa-text font-semibold text-sm">{{ store.chatActive.chatName }}</span>
              <span class="text-wa-text-muted text-xs">{{ t('chatSummaryMessages', { count: store.chatActive.msgCount }) }}</span>
            </div>
          </div>

          <!-- Footer -->
          <div class="flex gap-3 px-5 pb-5">
            <button class="wa-btn-ghost flex-1 text-sm" @click="toggleModal">{{ t('cancel') }}</button>
            <button
              class="flex-1 py-2 px-4 rounded-lg bg-red-600 hover:bg-red-700 text-white text-sm font-semibold transition-colors"
              @click="deleteChat"
            >
              {{ t('deleteNow') }}
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>
