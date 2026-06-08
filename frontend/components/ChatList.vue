<template>
  <!-- On mobile: hide when a chat is open. On desktop: always show (w-[380px]). -->
  <div
    id="chat-list-area"
    class="flex flex-col bg-wa-sidebar border-r border-wa-border wa-scroll overflow-y-auto flex-shrink-0"
    :class="[
      isMobile
        ? chatOpened ? 'hidden' : 'flex w-full'
        : 'flex w-[380px]'
    ]"
  >
    <!-- Sidebar header -->
    <div class="flex items-center justify-between px-4 py-2 bg-wa-header min-h-[59px] sticky top-0 z-10">
      <span class="text-wa-text font-semibold text-base">ChatVault</span>

      <!-- Action buttons (icon-style) -->
      <div class="flex items-center gap-1">
        <button
          class="p-2 rounded-full text-wa-icon hover:bg-wa-hover transition-colors"
          :title="t('enableBlur') + '/' + t('disableBlur')"
          @click="store.toggleBlur"
        >
          <!-- Eye / eye-off icon -->
          <svg v-if="store.blurEnabled" viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
            <path d="M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zm0 8c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm0-13C7 2 2.73 5.11 1 9.5 2.73 13.89 7 17 12 17s9.27-3.11 11-7.5C21.27 5.11 17 2 12 2z"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
          </svg>
        </button>

        <!-- More actions menu (create / import / export) -->
        <div class="relative" v-click-outside="() => (menuOpen = false)">
          <button
            class="p-2 rounded-full text-wa-icon hover:bg-wa-hover transition-colors"
            @click="menuOpen = !menuOpen"
            :title="t('more')"
          >
            <icon-three-dots />
          </button>

          <div
            v-if="menuOpen"
            class="absolute right-0 top-10 z-20 bg-wa-header rounded-lg shadow-2xl py-1 min-w-[180px] border border-wa-border"
          >
            <button
              class="flex w-full items-center gap-3 px-4 py-3 text-sm text-wa-text hover:bg-wa-hover transition-colors"
              @click="emitCreateNewChat"
            >
              <svg viewBox="0 0 24 24" class="w-4 h-4 text-wa-icon" fill="currentColor">
                <path d="M19 3H5c-1.1 0-2 .9-2 2v14l4-4h12c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 3c1.93 0 3.5 1.57 3.5 3.5S13.93 13 12 13s-3.5-1.57-3.5-3.5S10.07 6 12 6zm7 13H5l2.5-3c1.04.73 2.32 1.17 3.67 1.17 1.35 0 2.6-.44 3.67-1.17L17.5 19H19z"/>
              </svg>
              {{ t('createNewChat') }}
            </button>
            <button
              class="flex w-full items-center gap-3 px-4 py-3 text-sm text-wa-text hover:bg-wa-hover transition-colors"
              @click="emitDiskImport"
            >
              <svg viewBox="0 0 24 24" class="w-4 h-4 text-wa-icon" fill="currentColor">
                <path d="M20 6h-2.18c.07-.44.18-.88.18-1.36C18 2.54 15.42 1 12.5 1 9.58 1 7 2.54 7 4.64c0 .48.11.92.18 1.36H5C3.9 6 3 6.9 3 8v12c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-5 3l-3 3-3-3h2V9h2v3h2l-2 3z"/>
              </svg>
              {{ t('executeDiskImport') }}
            </button>
            <button
              class="flex w-full items-center gap-3 px-4 py-3 text-sm text-wa-text hover:bg-wa-hover transition-colors"
              @click="emitChatExport"
            >
              <svg viewBox="0 0 24 24" class="w-4 h-4 text-wa-icon" fill="currentColor">
                <path d="M19 9h-4V3H9v6H5l7 7 7-7zm-8 2V5h2v6h1.17L12 13.17 9.83 11H11zm-6 7h14v2H5v-2z"/>
              </svg>
              {{ t('executeChatExport') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Search bar -->
    <div class="px-3 py-2 bg-wa-sidebar">
      <div class="relative">
        <svg
          class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-wa-text-muted pointer-events-none"
          viewBox="0 0 24 24" fill="currentColor"
        >
          <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
        </svg>
        <input
          type="text"
          class="wa-search-input text-sm"
          :placeholder="t('searchChatsPlaceholder', { defaultValue: 'Pesquisar ou começar um novo chat' })"
          v-model="chatSearch"
        />
      </div>
    </div>

    <!-- Chat list -->
    <div class="flex-1">
      <template v-for="item in filteredChats" :key="item.chatId">
        <chat-item :item="item" @update:chat-active="emitThisChatActive" />
      </template>

      <div
        v-if="filteredChats.length === 0"
        class="flex flex-col items-center justify-center py-16 text-wa-text-muted text-sm gap-2"
      >
        <svg viewBox="0 0 24 24" class="w-12 h-12 opacity-30" fill="currentColor">
          <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/>
        </svg>
        <span>Nenhuma conversa encontrada</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useMainStore } from '~/store'
import { useUiText } from '~/composables/useUiText'

const store = useMainStore()
const { t } = useUiText()

const props = defineProps(['chats', 'mobile'])
const emit = defineEmits(['update:chat-active', 'create:chat', 'update:disk-import', 'export:chat'])

const chatOpened = computed(() => store.chatActive?.chatId != null)
const menuOpen = ref(false)
const chatSearch = ref('')

const filteredChats = computed(() => {
  if (!props.chats) return []
  if (!chatSearch.value.trim()) return props.chats
  const q = chatSearch.value.toLowerCase()
  return props.chats.filter((c: any) =>
    c.chatName?.toLowerCase().includes(q) || c.content?.toLowerCase().includes(q)
  )
})

function emitThisChatActive(item: any) {
  menuOpen.value = false
  emit('update:chat-active', item)
}

function emitCreateNewChat() {
  menuOpen.value = false
  emit('create:chat')
}

function emitChatExport() {
  menuOpen.value = false
  store.chatExited()
  emit('export:chat')
}

async function emitDiskImport() {
  menuOpen.value = false
  store.loading = true
  await useFetch(useRuntimeConfig().public.api.importFromDisk, { method: 'post' })
  store.loading = false
  emit('update:disk-import')
}

// Close menu when clicking outside
const vClickOutside = {
  mounted(el: HTMLElement, binding: any) {
    el._clickOutside = (event: MouseEvent) => {
      if (!el.contains(event.target as Node)) {
        binding.value(event)
      }
    }
    document.addEventListener('click', el._clickOutside)
  },
  unmounted(el: HTMLElement) {
    document.removeEventListener('click', el._clickOutside)
  },
}
</script>
