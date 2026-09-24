<template>
  <div id="message-area"
       class="message-area flex-column col-12 h-100 overflow-auto"
       :class="dynamicClass"
       ref="messagesAreaElement"
       @scroll="onScroll"
  >

    <message-area-nav-bar/>
    <div id="infinite-list" class="message-list d-flex flex-column">
      <div v-if="isLoadingMore" class="load-more-indicator align-self-center my-2">
        <div class="spinner-border spinner-border-sm" role="status"></div>
      </div>
      <template v-for="(item, index) in datedMessages" :key="item.type === 'date' ? `d-${item.label}` : `m-${item.message.id}`">
        <div v-if="item.type === 'date'" class="date-separator">
          <span class="date-pill">{{ item.label }}</span>
        </div>
        <message-item v-else :message="item.message"/>
      </template>
    </div>

  </div>
</template>

<script setup lang="ts">
import MessageItem from "~/components/MessageItem.vue";
import {useMainStore} from "~/store";

const store = useMainStore()
const props = defineProps(['mobile'])
const messagesAreaElement = ref(null)
const isLoadingMore = ref(false)

const moreMessagesPath = computed(() => { return store.moreMessagesPath });

const {data: response, refresh} = await useFetch(moreMessagesPath)

const content = computed(() => {
  return response?.value?.content ?? []
})

const messages = computed(() => store.messages)

const datedMessages = computed(() => {
  const result: Array<{ type: 'date'; label: string } | { type: 'message'; message: any }> = []
  const list = messages.value as any[]
  list.forEach((message, index) => {
    const previous = index === 0 ? null : list[index - 1]
    const label = dateLabelFor(message, previous)
    if (label) {
      result.push({ type: 'date', label })
    }
    result.push({ type: 'message', message })
  })
  return result
})

const hasNextPages = computed(() => {
  if (response?.value) {
    return !response.value.last
  } else {
    return false
  }
})

const dynamicClass = computed(() => {
  return {
    'd-none': props.mobile && (store.chatActive.chatId == null || store.chatConfigOpen),
  }
})

function scrollBottom() {
  if (messagesAreaElement.value) {
    messagesAreaElement.value.scrollTo({
      top: messagesAreaElement.value.scrollHeight,
      behavior: 'auto'
    })
  }
}

function loadMoreMessages() {
  if (isLoadingMore.value || !hasNextPages.value) return
  isLoadingMore.value = true
  store.toNextPage()
}

function onScroll() {
  const el = messagesAreaElement.value
  if (!el) return
  if (el.scrollTop <= 80) {
    loadMoreMessages()
  }
}

function startOfDay(date: Date): number {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime()
}

function dateLabelFor(message: any, previous: any): string | null {
  const current = new Date(message.createdAt)
  if (previous) {
    const prev = new Date(previous.createdAt)
    if (startOfDay(current) === startOfDay(prev)) {
      return null
    }
  }
  return formatDateLabel(current)
}

function formatDateLabel(date: Date): string {
  const now = new Date()
  const diffDays = Math.round((startOfDay(now) - startOfDay(date)) / 86400000)

  if (diffDays === 0) return 'Hoje'
  if (diffDays === 1) return 'Ontem'

  if (diffDays > 1 && isSameWeek(date, now)) {
    const weekdays = ['domingo', 'segunda-feira', 'terça-feira', 'quarta-feira', 'quinta-feira', 'sexta-feira', 'sábado']
    return weekdays[date.getDay()]
  }

  const dd = String(date.getDate()).padStart(2, '0')
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  return `${dd}/${mm}/${date.getFullYear()}`
}

function isSameWeek(date: Date, now: Date): boolean {
  const mondayTime = (d: Date) => {
    const start = new Date(d.getFullYear(), d.getMonth(), d.getDate())
    const dow = start.getDay()
    const diff = dow === 0 ? 6 : dow - 1
    start.setDate(start.getDate() - diff)
    return start.getTime()
  }
  return mondayTime(date) === mondayTime(now)
}

watch(
    () => store.chatActive.chatId,
    (chatId) => {
      store.clearMessages()
    }
)

watch(
    () => messages.value.length,
    (sizeOfMessages) => {
      if (sizeOfMessages === 0) {
        refresh()
      }
    }
)

watch(content, async (newContent) => {
  const el = messagesAreaElement.value
  const prevScrollHeight = el?.scrollHeight ?? 0
  const prevScrollTop = el?.scrollTop ?? 0
  const wasInitial = messages.value.length === 0

  const incoming = [...newContent].reverse().map((it: any) => store.toChatMessage(it))
  store.updateMessages([...incoming, ...messages.value])
  await nextTick()
  isLoadingMore.value = false

  if (wasInitial) {
    scrollBottom()
    if (el && el.scrollHeight <= el.clientHeight && hasNextPages.value) {
      loadMoreMessages()
    }
  } else {
    const addedHeight = (el?.scrollHeight ?? 0) - prevScrollHeight
    if (el) el.scrollTop = prevScrollTop + addedHeight
  }
})

</script>

<style scoped>
.message-area {
  background: var(--wa-bg-5);
}

@media (min-width: 768px) {
  .message-area {
    flex: 1 1 0%;
    width: auto;
    max-width: none;
    min-width: 0;
  }
}

.message-list {
  padding: 12px 4% 12px 4%;
  min-height: 100%;
}

.load-more-indicator {
  display: flex;
  justify-content: center;
  padding: 4px 0;
}

.load-more-indicator .spinner-border {
  color: var(--wa-text-2);
}

.date-separator {
  position: sticky;
  top: 69px;
  z-index: 3;
  display: flex;
  justify-content: center;
  margin: 10px 0;
}

.date-pill {
  background: var(--wa-bg-3);
  color: var(--wa-text-2);
  font-size: 12px;
  padding: 5px 12px;
  border-radius: 8px;
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.3);
  text-transform: capitalize;
  white-space: nowrap;
}
</style>