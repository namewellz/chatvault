<script setup lang="ts">
import { useMainStore } from '~/store'
import { computed, reactive, watch } from 'vue'
import { LOCALE_DISPLAY_NAMES, SUPPORTED_LOCALES } from '~/types/localization'
import { useDateFormatting } from '~/composables/useDateFormatting'
import { useUiText } from '~/composables/useUiText'

const emit = defineEmits(['refresh:page'])

const store = useMainStore()
const { systemLocale } = useDateFormatting()
const { t } = useUiText()

const chatConfig = reactive({
  chatName: store.chatActive.chatName,
  editChatName: false,
  invalidPageSize: false,
  showGallery: false,
})

watch(() => store.chatActive.chatId, () => {
  chatConfig.chatName = store.chatActive.chatName
})

const toggleChatConfig = () => (store.chatConfigOpen = !store.chatConfigOpen)
const toggleGallery = () => { chatConfig.showGallery = !chatConfig.showGallery }

const toggleChatName = async () => {
  chatConfig.editChatName = !chatConfig.editChatName
  if (!chatConfig.editChatName && chatConfig.chatName !== store.chatActive.chatName) {
    await updateChatName()
  }
}

const updateChatName = async () => {
  const path = useRuntimeConfig()
    .public.api.updateChatNameByChatId
    .replace(':chatId', store.chatActive.chatId.toString())
    .replace(':chatName', chatConfig.chatName)
  await $fetch(path, { method: 'PATCH' })
  store.chatActive.chatName = chatConfig.chatName
}

const validatedPageSize = (event: Event) => {
  event.preventDefault()
  const input = event.target as HTMLInputElement
  const updated = store.updatePageSize(Number(input.value))
  chatConfig.invalidPageSize = !updated
}
</script>

<template>
  <div
    class="flex flex-col bg-wa-panel border-l border-wa-border wa-scroll overflow-y-auto flex-shrink-0"
    :class="['w-full sm:w-[380px]']"
  >
    <!-- Gallery view -->
    <gallery v-if="chatConfig.showGallery">
      <button
        class="m-3 p-2 rounded-full text-wa-icon hover:bg-wa-hover transition-colors self-start"
        @click="toggleGallery"
      >
        <svg viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
          <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
        </svg>
      </button>
    </gallery>

    <template v-else>
      <!-- Header -->
      <div class="flex items-center gap-3 px-4 py-3 bg-wa-header min-h-[59px] border-b border-wa-border">
        <button
          class="p-1.5 rounded-full text-wa-icon hover:bg-wa-hover transition-colors"
          @click="toggleChatConfig"
        >
          <svg viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
          </svg>
        </button>
        <span class="text-wa-text font-semibold text-[15px]">{{ t('chatSettingsTitle') }}</span>
      </div>

      <!-- Profile image + delete -->
      <div class="flex items-center justify-between px-4 py-4 border-b border-wa-border">
        <profile-image-uploader />
        <chat-deleter @refresh:page="() => emit('refresh:page')" />
      </div>

      <!-- Chat name -->
      <div class="mx-3 mt-3 bg-wa-header/50 rounded-xl p-3 border border-wa-border">
        <p class="text-wa-text-muted text-[11px] uppercase tracking-wider mb-2">{{ t('chatNameLabel') }}</p>
        <div class="flex items-center gap-2">
          <input
            type="text"
            :disabled="!chatConfig.editChatName"
            class="wa-input flex-1 text-sm"
            v-model="chatConfig.chatName"
          />
          <button
            class="p-2 rounded-full text-wa-icon hover:bg-wa-hover transition-colors flex-shrink-0"
            @click="toggleChatName"
          >
            <icon-check v-if="chatConfig.editChatName" />
            <icon-pencil-square v-else />
          </button>
        </div>
      </div>

      <!-- Active author -->
      <div class="mx-3 mt-3 bg-wa-header/50 rounded-xl p-3 border border-wa-border">
        <label class="text-wa-text-muted text-[11px] uppercase tracking-wider block mb-2">
          {{ t('activeAuthorLabel') }}
        </label>
        <select class="wa-input text-sm" v-model="store.authorActive">
          <option v-for="option in store.authors" :key="option" :value="option">{{ option }}</option>
        </select>
      </div>

      <!-- Page size -->
      <div class="mx-3 mt-3 bg-wa-header/50 rounded-xl p-3 border border-wa-border">
        <label class="text-wa-text-muted text-[11px] uppercase tracking-wider block mb-2">
          {{ t('pageSizeLabel') }}
        </label>
        <input
          class="wa-input text-sm"
          type="number" max="2000" min="1" placeholder="20"
          @input="validatedPageSize"
        />
        <p v-if="chatConfig.invalidPageSize" class="text-red-400 text-xs mt-1">{{ t('pageSizeInvalid') }}</p>
      </div>

      <!-- Locale -->
      <div class="mx-3 mt-3 bg-wa-header/50 rounded-xl p-3 border border-wa-border">
        <label class="text-wa-text-muted text-[11px] uppercase tracking-wider block mb-2">
          {{ t('dateFormatLocaleLabel') }}
        </label>
        <select class="wa-input text-sm" v-model="store.userLocale">
          <option v-for="locale in SUPPORTED_LOCALES" :key="locale" :value="locale">
            {{ LOCALE_DISPLAY_NAMES[locale] }}<span v-if="locale === 'auto'"> ({{ systemLocale }})</span>
          </option>
        </select>
        <p class="text-wa-text-muted text-xs mt-1.5">{{ t('dateFormatLocaleHelp') }}</p>
      </div>

      <!-- Actions -->
      <div class="mx-3 mt-3 mb-6 flex flex-col gap-2">
        <import-export-chat />
        <button class="wa-btn-ghost text-sm py-2.5" @click="toggleGallery">
          {{ t('openMediaGallery') }} →
        </button>
      </div>
    </template>
  </div>
</template>
