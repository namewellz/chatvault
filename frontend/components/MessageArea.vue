<template>
  <div
    id="message-area"
    class="flex flex-col flex-1 min-w-0 overflow-hidden"
    :class="dynamicClass"
  >
    <!-- Chat not open: empty state -->
    <div
      v-if="!store.chatActive.chatId"
      class="flex flex-col items-center justify-center h-full bg-wa-bg text-wa-text-muted gap-4"
    >
      <svg viewBox="0 0 24 24" class="w-20 h-20 opacity-10" fill="currentColor">
        <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/>
      </svg>
      <p class="text-base opacity-40">Selecione uma conversa</p>
    </div>

    <!-- Chat open -->
    <template v-else>
      <!-- Header navbar -->
      <message-area-nav-bar :mobile="props.mobile" />

      <!-- Messages container -->
      <div
        id="infinite-list"
        ref="messagesAreaElement"
        class="flex-1 overflow-y-auto wa-scroll px-4 py-3 wa-chat-bg"
        style="background-color: #0B141A;"
      >
        <!-- Jump mode header -->
        <div v-if="isJumpMode" class="flex flex-col items-center gap-2 mb-3 sticky top-1 z-10">
          <div class="flex items-center gap-2">
            <span class="wa-jump-badge">{{ t('jumpModeActive') }}</span>
            <span v-if="jumpModeDate" class="text-wa-text-muted text-xs">{{ jumpModeDate }}</span>
          </div>
          <button
            v-if="hasOlderMessages"
            class="wa-btn-ghost text-xs px-3 py-1.5"
            @click="loadOlderMessages"
          >
            ↑ {{ t('loadOlderMessages') }}
          </button>
        </div>

        <!-- Load more (non-jump mode) -->
        <div v-if="!isJumpMode && hasOlderMessages" class="flex justify-center mb-3">
          <button class="wa-btn-ghost text-xs px-4 py-1.5" @click="loadOlderMessages">
            {{ t('loadMoreMessages') }}
          </button>
        </div>

        <!-- Messages + date dividers -->
        <template v-for="item in messagesWithDividers" :key="item.key">
          <div v-if="item.type === 'divider'" class="wa-date-divider my-2">
            <span>{{ item.label }}</span>
          </div>
          <message-item
            v-else
            :message="item.data"
            :highlight-until-date="store.highlightUntilDate"
            :highlight-message-id="store.highlightMessageId"
            :enable-jump="isSearchMode"
            @jump="handleMessageJump"
          />
        </template>

        <!-- Spacer at bottom -->
        <div class="h-2" />

        <!-- Jump mode: load newer / back to latest -->
        <div v-if="isJumpMode" class="flex flex-col items-center gap-2 mt-2 sticky bottom-2 z-10">
          <button
            v-if="hasNewerMessages"
            class="wa-btn-ghost text-xs px-3 py-1.5"
            @click="loadNewerMessages"
          >
            ↓ {{ t('loadNewerMessages') }}
          </button>
          <button class="wa-btn text-xs px-4 py-1.5" @click="returnToLatest">
            {{ t('backToLatest') }}
          </button>
        </div>
      </div>

      <!-- Decorative input bar (read-only app, no send) -->
      <div class="flex items-center gap-3 px-4 py-2 bg-wa-header min-h-[62px] flex-shrink-0">
        <div class="flex-1 bg-wa-search rounded-full px-5 py-2.5 text-wa-text-muted text-sm select-none">
          {{ t('readOnlyChat', { defaultValue: 'Visualização de backup — modo somente leitura' }) }}
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import MessageItem from '~/components/MessageItem.vue'
import { useMainStore } from '~/store'
import { useUiText } from '~/composables/useUiText'
import { useDateFormatting } from '~/composables/useDateFormatting'

const store = useMainStore()
const { t } = useUiText()
const { formatDateFull } = useDateFormatting()
const props = defineProps(['mobile'])
const messagesAreaElement = ref<HTMLElement | null>(null)

const moreMessagesPath = computed(() => store.moreMessagesPath)
const { data: response, refresh } = await useFetch(moreMessagesPath)

const content = computed(() => response?.value?.content ?? [])
const messageViewMode = computed(() => store.messageViewMode)

const messages = computed(() => {
  if (messageViewMode.value === 'context') return store.contextMessages
  if (messageViewMode.value === 'search') return store.searchResults
  return store.messages
})

const isSearchMode = computed(() => messageViewMode.value === 'search')
const isJumpMode = computed(() => store.paginationMode === 'jump')

const jumpModeDate = computed(() => {
  if (!store.highlightUntilDate) return ''
  const date = new Date(store.highlightUntilDate)
  if (Number.isNaN(date.getTime())) return store.highlightUntilDate
  return formatDateFull(date)
})

const hasOlderMessages = computed(() => {
  if (isSearchMode.value) return false
  if (isJumpMode.value) return store.jumpHasMoreOlder
  return response?.value ? !response.value.last : false
})

const hasNewerMessages = computed(() => {
  if (!isJumpMode.value) return false
  return store.jumpHasMoreNewer
})

const dynamicClass = computed(() => ({
  hidden: props.mobile && (store.chatActive.chatId == null || store.chatConfigOpen),
}))

// ── Date dividers ────────────────────────────────────────────────
const messagesWithDividers = computed(() => {
  const result: Array<
    { key: string; type: 'divider'; label: string } |
    { key: string; type: 'message'; data: any }
  > = []

  let lastDate: string | null = null

  for (const msg of messages.value) {
    const d = new Date(msg.createdAt)
    const dateStr = `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`

    if (dateStr !== lastDate) {
      result.push({ key: `div-${dateStr}`, type: 'divider', label: formatDividerDate(d) })
      lastDate = dateStr
    }

    result.push({ key: `msg-${msg.id}`, type: 'message', data: msg })
  }

  return result
})

function formatDividerDate(date: Date): string {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  const msgDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())

  if (msgDay.getTime() === today.getTime()) return 'Hoje'
  if (msgDay.getTime() === yesterday.getTime()) return 'Ontem'

  return date.toLocaleDateString(undefined, { day: '2-digit', month: 'long', year: 'numeric' })
}

// ── Scroll helpers ────────────────────────────────────────────────
function scrollBottom() {
  if (messagesAreaElement.value && store.nextPage === 0) {
    messagesAreaElement.value.scrollTo({ top: messagesAreaElement.value.scrollHeight, behavior: 'smooth' })
  }
}

function scrollToFirstMessage() {
  if (messagesAreaElement.value && messages.value.length > 0) {
    nextTick(() => {
      const firstEl = messagesAreaElement.value?.querySelector('.wa-message-row')
      if (firstEl) {
        const top = (firstEl as HTMLElement).offsetTop - messagesAreaElement.value!.clientHeight / 3
        messagesAreaElement.value!.scrollTo({ top: Math.max(0, top), behavior: 'smooth' })
      }
    })
  }
}

// ── Actions ───────────────────────────────────────────────────────
function loadOlderMessages() {
  store.loadOlderMessages()
}

function loadNewerMessages() {
  if (!messagesAreaElement.value) { store.loadNewerMessages(); return }
  const prevScrollTop = messagesAreaElement.value.scrollTop
  const prevScrollHeight = messagesAreaElement.value.scrollHeight
  store.loadNewerMessages().then(async () => {
    await nextTick()
    const newHeight = messagesAreaElement.value?.scrollHeight ?? prevScrollHeight
    messagesAreaElement.value?.scrollTo({ top: prevScrollTop + (newHeight - prevScrollHeight), behavior: 'auto' })
  })
}

function returnToLatest() {
  store.resetPaginationState()
  store.clearMessages()
  refresh()
}

function handleMessageJump(message: { id: number; createdAt: string }) {
  if (!isSearchMode.value || !store.chatActive.chatId) return
  store.jumpToDate(store.chatActive.chatId, message.createdAt, message.id)
}

// ── Watchers ──────────────────────────────────────────────────────
watch(() => store.chatActive.chatId, () => {
  store.resetPaginationState()
  store.clearMessages()
})

watch(
  () => messages.value.length,
  (size) => {
    if (messageViewMode.value !== 'default' || isJumpMode.value) return
    if (size === 0) refresh()
  }
)

watch(content, async (newContent) => {
  if (messageViewMode.value !== 'default' || isJumpMode.value) return
  store.updateMessages([...newContent.reverse().map((it: any) => store.toChatMessage(it)), ...messages.value])
  await nextTick()
  store.highlightUntilDate ? scrollToFirstMessage() : scrollBottom()
})

watch(() => store.highlightUntilDate, async (val) => {
  if (!val || messages.value.length === 0) return
  await nextTick()
  scrollToFirstMessage()
})
</script>
