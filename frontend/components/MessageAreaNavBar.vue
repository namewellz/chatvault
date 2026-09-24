<template>
  <div id="navbar"
       class="sticky-top d-flex align-items-center px-3 m-0"
       v-if="chatActive">
    <div :class="{ 'blur-sensitive': store.blurEnabled }" class="chat-info-header d-flex align-items-center flex-grow-1">
      <a href="#" class="back-link" @click="exitThisChat">
        <rotable-arrow-icon/>
      </a>
      <a href="#" class="avatar-link" @click="() => toggleOpenChatConfig()">
        <profile-image :id="store.chatActive.chatId"/>
      </a>
      <div class="d-flex flex-column chat-info-text" role="button" @click="() => toggleOpenChatConfig()">
        <div class="chat-name" id="name">{{ store.chatActive.chatName }}</div>
        <div class="chat-detail" id="details">{{ store.chatActive.msgCount }} messages</div>
      </div>

    </div>
    <search-bar :chatId="store.chatActive.chatId" @search="handleSearch" />
    <button type="button" class="wa-icon-btn" role="button" @click="() => toggleOpenChatConfig()">
      <icon-three-dots class="self"/>
    </button>

  </div>
</template>

<script setup lang="ts">
import {useMainStore} from '~/store';

const store = useMainStore();
const chatActive = computed(() => store.chatActive.chatId > 0);

function exitThisChat() {
  store.chatExited();
}

function toggleOpenChatConfig() {
  store.chatConfigOpen = !store.chatConfigOpen;
}

function handleSearch({ query, chatId }: { query: string; chatId: string | null }) {
  console.log("Search query:", query, "Chat ID:", chatId);
  // todo: post search
}

</script>

<style scoped>
#navbar {
  background: var(--wa-bg-2);
  border-bottom: 1px solid var(--wa-border-1);
  height: 59px;
  gap: 12px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: var(--wa-text-3);
  margin-left: -8px;
  transition: background 0.15s ease;
}

.back-link:hover {
  background: var(--wa-hover);
  color: var(--wa-text-1);
}

.avatar-link {
  margin: 0 10px 0 4px;
  line-height: 0;
}

.chat-info-text {
  min-width: 0;
}

.chat-name {
  color: var(--wa-text-1);
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-detail {
  color: var(--wa-text-2);
  font-size: 12.5px;
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

.blur-sensitive {
  filter: blur(6px);
  transition: filter 0.3s ease-in-out;
}

#navbar:hover .blur-sensitive {
  filter: none;
}
</style>
