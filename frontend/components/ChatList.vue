<template>
  <div id="chat-list-area"
       class="sidebar col-12 col-md-3 h-100 p-0 d-flex flex-column"
       :class="dynamicClass">
    <div class="sidebar-header d-flex align-items-center justify-content-between">
      <span class="sidebar-title">Chats</span>
      <div class="sidebar-actions d-flex align-items-center">
        <button type="button" class="wa-icon-btn" title="Create new chat" @click="emitCreateNewChat">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true">
            <path d="M16.862 4.487l1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L10.582 16.07a4.5 4.5 0 0 1-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 0 1 1.13-1.897l8.932-8.931zm0 0L19.5 7.125"/>
          </svg>
        </button>
        <button type="button" class="wa-icon-btn" title="Import from disk" @click="emitDiskImport">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5M16.5 12 12 16.5m0 0L7.5 12m4.5 4.5V3"/>
          </svg>
        </button>
        <button type="button" class="wa-icon-btn" title="Export all chats" @click="emitChatExport">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5m-13.5-9L12 3m0 0 4.5 4.5M12 3v13.5"/>
          </svg>
        </button>
        <button type="button" class="wa-icon-btn" :title="store.blurEnabled ? 'Disable blur' : 'Enable blur'" @click="store.toggleBlur">
          <svg v-if="store.blurEnabled" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 0 0 1.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.451 10.451 0 0 1 12 4.5c4.756 0 8.773 3.162 10.065 7.498a10.522 10.522 0 0 1-4.293 5.774M6.228 6.228 3 3m3.228 3.228 3.65 3.65m7.894 7.894L21 21m-3.228-3.228-3.65-3.65m0 0a3 3 0 1 0-4.243-4.243m4.242 4.242L9.88 9.88"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 0 1 0-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/>
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
          </svg>
        </button>
      </div>
    </div>
    <div class="conversation-list flex-grow-1 overflow-auto">
      <template v-for="item in chats" :key="item.chatId">
        <chat-item :item="item"
                   @update:chat-active="emitThisChatActive"
        />
      </template>
    </div>

  </div>
</template>
<script setup lang="ts">

import {useMainStore} from "~/store";

const store = useMainStore()

const props = defineProps(['chats', 'mobile'])
const emit = defineEmits(['update:chat-active', 'create:chat', 'update:disk-import', 'export:chat'])

const chatOpened = computed(() => store.chatActive?.chatId != null)
const dynamicClass = computed(() => {
  return {
    'd-none': props.mobile && chatOpened.value,
    'd-flex': props.mobile && !chatOpened.value
  }
})

function emitThisChatActive(item: any) {
  emit('update:chat-active', item)
}

function emitCreateNewChat() {
  emit('create:chat')
}

function emitChatExport() {
  exitThisChat()
  emit('export:chat')
}

async function emitDiskImport() {
  store.loading = true
  await useFetch(useRuntimeConfig().public.api.importFromDisk, {method: 'post'})
  store.loading = false
  emit('update:disk-import')
}

function exitThisChat() {
  store.chatExited()
}

</script>


<style scoped>
.sidebar {
  background: var(--wa-bg-1);
  border-right: 1px solid var(--wa-border-1);
  max-width: 100%;
}

.sidebar-header {
  height: 59px;
  padding: 10px 16px;
  background: var(--wa-bg-2);
  border-bottom: 1px solid var(--wa-border-1);
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--wa-text-1);
}

.wa-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--wa-text-3);
  cursor: pointer;
  transition: background 0.15s ease;
}

.wa-icon-btn:hover {
  background: var(--wa-hover);
  color: var(--wa-text-1);
}

.conversation-list {
  background: var(--wa-bg-1);
}
</style>
