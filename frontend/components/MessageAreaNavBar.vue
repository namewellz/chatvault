<template>
  <div
    v-if="chatActive"
    class="flex items-center gap-3 px-3 bg-wa-header min-h-[59px] border-b border-wa-border flex-shrink-0 blur-parent"
  >
    <!-- Back arrow (mobile) -->
    <button
      v-if="isMobile"
      class="p-1.5 rounded-full text-wa-icon hover:bg-wa-hover transition-colors"
      @click="exitThisChat"
    >
      <svg viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
        <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
      </svg>
    </button>

    <!-- Avatar + name (clickable → config panel) -->
    <button
      class="flex items-center gap-3 flex-1 min-w-0 text-left hover:opacity-90 transition-opacity"
      :class="{ 'blur-sensitive': store.blurEnabled }"
      @click="toggleOpenChatConfig"
    >
      <profile-image :id="store.chatActive.chatId" class="w-10 h-10 rounded-full flex-shrink-0" />
      <div class="min-w-0">
        <p class="text-wa-text font-semibold text-[15px] truncate m-0 leading-tight">
          {{ store.chatActive.chatName }}
        </p>
        <p class="text-wa-text-muted text-[12px] m-0 leading-tight">
          {{ t('messagesCount', { count: store.chatActive.msgCount }) }}
        </p>
      </div>
    </button>

    <!-- Search bar (inline, collapsible) -->
    <div class="flex items-center gap-2">
      <div
        class="flex items-center overflow-hidden transition-all duration-300"
        :class="searchOpen ? 'w-48 sm:w-64' : 'w-0'"
      >
        <div class="relative w-full">
          <svg
            class="absolute left-2 top-1/2 -translate-y-1/2 w-4 h-4 text-wa-text-muted pointer-events-none"
            viewBox="0 0 24 24" fill="currentColor"
          >
            <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
          </svg>
          <input
            ref="searchInputRef"
            type="text"
            class="wa-search-input text-sm pl-8 py-1.5"
            :placeholder="t('searchPlaceholder')"
            v-model="store.searchQuery"
            @input="onSearch"
          />
        </div>
      </div>

      <!-- Search toggle button -->
      <button
        class="p-2 rounded-full text-wa-icon hover:bg-wa-hover transition-colors"
        :title="t('searchPlaceholder')"
        @click="toggleSearch"
      >
        <svg v-if="!searchOpen" viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
          <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
        </svg>
        <svg v-else viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
          <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
        </svg>
      </button>

      <!-- Calendar button -->
      <button
        class="p-2 rounded-full text-wa-icon hover:bg-wa-hover transition-colors"
        :title="t('openCalendarTitle')"
        @click="toggleCalendar"
      >
        <svg viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
          <path d="M7 2a1 1 0 0 1 1 1v1h8V3a1 1 0 1 1 2 0v1h1.5A2.5 2.5 0 0 1 22 6.5v12A2.5 2.5 0 0 1 19.5 21h-15A2.5 2.5 0 0 1 2 18.5v-12A2.5 2.5 0 0 1 4.5 4H6V3a1 1 0 0 1 1-1Zm12.5 8h-15a.5.5 0 0 0-.5.5v8a.5.5 0 0 0 .5.5h15a.5.5 0 0 0 .5-.5v-8a.5.5 0 0 0-.5-.5ZM4.5 6a.5.5 0 0 0-.5.5V8h16V6.5a.5.5 0 0 0-.5-.5h-15Z"/>
        </svg>
      </button>

      <!-- Three dots / more -->
      <button
        class="p-2 rounded-full text-wa-icon hover:bg-wa-hover transition-colors"
        @click="toggleOpenChatConfig"
      >
        <icon-three-dots />
      </button>
    </div>

    <!-- Date Picker Modal -->
    <date-picker-modal
      :is-open="store.calendarOpen"
      :message-statistics="store.messageStatistics"
      :current-calendar-month="store.currentCalendarMonth"
      :statistics-loading="store.statisticsLoading"
      :user-locale="store.userLocale"
      @close="handleCalendarClose"
      @select-date="handleCalendarDateSelect"
      @month-changed="handleCalendarMonthChange"
    />
  </div>
</template>

<script setup lang="ts">
import { useMainStore } from '~/store'
import { useUiText } from '~/composables/useUiText'

const store = useMainStore()
const { t } = useUiText()
const props = defineProps({ mobile: { type: Boolean, default: false } })

const chatActive = computed(() => store.chatActive.chatId > 0)
const isMobile = computed(() => props.mobile)
const searchOpen = ref(false)
const searchInputRef = ref<HTMLInputElement | null>(null)

function exitThisChat() {
  store.chatExited()
}

function toggleOpenChatConfig() {
  store.chatConfigOpen = !store.chatConfigOpen
}

function toggleSearch() {
  searchOpen.value = !searchOpen.value
  if (!searchOpen.value) {
    store.closeSearch()
    store.searchQuery = ''
  } else {
    nextTick(() => searchInputRef.value?.focus())
  }
}

function onSearch() {
  store.clearMessages()
  emit('search', { query: store.searchQuery || '', chatId: store.chatActive.chatId })
  if (store.searchQuery?.trim()) {
    store.performSearch(store.searchQuery, store.chatActive.chatId)
    store.searchOpen = false
  } else {
    store.closeSearch()
  }
}

function toggleCalendar() {
  if (store.calendarOpen) {
    store.closeCalendar()
  } else {
    store.openCalendar()
    store.fetchMessageStatistics(store.chatActive.chatId, store.currentCalendarMonth)
  }
}

function handleCalendarClose() {
  store.closeCalendar()
}

function handleCalendarDateSelect(dateStr: string) {
  store.closeCalendar()
  store.jumpToDate(store.chatActive.chatId, dateStr)
}

function handleCalendarMonthChange(date: Date) {
  store.setCalendarMonth(date)
  store.fetchMessageStatistics(store.chatActive.chatId, date)
}

const emit = defineEmits(['search'])

watch(() => store.chatActive.chatId, () => {
  searchOpen.value = false
  store.searchQuery = ''
})
</script>
