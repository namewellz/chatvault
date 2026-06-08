<template>
  <div class="flex h-screen w-screen overflow-hidden bg-wa-bg text-wa-text font-sans" ref="indexRef">

    <!-- Loading overlay -->
    <div
      v-if="store.loading"
      class="absolute inset-0 z-50 flex items-center justify-center bg-black/50"
    >
      <div class="flex items-center gap-3 rounded-xl bg-wa-header px-6 py-4 shadow-2xl">
        <div class="wa-spinner"></div>
        <span class="text-wa-text text-sm">{{ t('loading') }}</span>
      </div>
    </div>

    <!-- New chat dialog -->
    <new-chat-uploader
      v-if="createChatAction"
      @update:chats="refreshPage"
      @exit:dialog="() => (createChatAction = false)"
    />

    <!-- Export dialog -->
    <chat-exporter
      v-else-if="exportChatAction"
      allow-download-all="true"
      @exit:dialog="() => (exportChatAction = false)"
    />

    <!-- Main 3-column layout -->
    <template v-else>
      <!-- Left: Chat list sidebar -->
      <chat-list
        :chats="chats"
        :mobile="isMobile"
        @create:chat="createNewChat"
        @update:chat-active="updateChatActive"
        @export:chat="exportChat"
        @update:disk-import="refreshPage"
      />

      <!-- Center: Message area -->
      <message-area :mobile="isMobile" />

      <!-- Right: Config panel (optional) -->
      <chat-config v-if="store.chatConfigOpen" @refresh:page="() => refresh()" />
    </template>

    <!-- Version tag -->
    <span
      class="absolute bottom-1 right-2 text-[11px] text-wa-text-muted opacity-40 pointer-events-none select-none"
    >
      ChatVault v{{ appVersion }}
    </span>
  </div>
</template>

<script setup lang="ts">
import ChatList from '~/components/ChatList.vue'
import MessageArea from '~/components/MessageArea.vue'
import ChatConfig from '~/components/ChatConfig.vue'
import { useMainStore } from '~/store'
import { useUiText } from '~/composables/useUiText'

const store = useMainStore()
const { t } = useUiText()
const listChatsAPIUrl = useRuntimeConfig().public.api.listChats
const getAppVersionAPIUrl = useRuntimeConfig().public.api.appVersion
const { data: chats, refresh } = await useFetch(listChatsAPIUrl)
const { data: versionData } = await useFetch(getAppVersionAPIUrl)
const appVersion = computed(() => versionData.value?.version ?? '')
const isMobile = ref(false)
const indexRef = ref<HTMLElement | null>(null)
const createChatAction = ref(false)
const exportChatAction = ref(false)

function checkWindowSize() {
  if (indexRef.value) {
    isMobile.value = indexRef.value.offsetWidth <= 640
  }
}

function refreshPage() {
  store.loading = true
  createChatAction.value = false
  exportChatAction.value = false
  refresh()
  store.loading = false
}

function createNewChat() {
  createChatAction.value = true
}

function exportChat() {
  exportChatAction.value = true
}

function updateChatActive(item: any) {
  store.openChat(item)
}

onMounted(() => {
  checkWindowSize()
  window.addEventListener('resize', checkWindowSize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkWindowSize)
})
</script>
