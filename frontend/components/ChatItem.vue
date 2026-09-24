<template>
  <div class="chat-item d-flex flex-row align-items-center w-100"
       :class="{ active: isActive }"
       @click="emitThisChatActive()">
    <profile-image :id="item.chatId"/>
    <div
        class="chat-item-body flex-grow-1"
        :class="{ 'blur-sensitive': store.blurEnabled }"
    >
      <div class="chat-item-top d-flex justify-content-between align-items-baseline">
        <span class="chat-item-name">{{ item.chatName }}</span>
        <message-created-at :date="item.msgCreatedAt" class="chat-item-time" />
      </div>
      <div class="chat-item-preview">
        <span class="chat-item-last">{{ item.content }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useMainStore } from "~/store";

const store = useMainStore();
const props = defineProps(["item"]);
const emit = defineEmits(["update:chat-active"]);

const isActive = computed(() => store.chatActive?.chatId === props.item.chatId);

function emitThisChatActive() {
  emit("update:chat-active", props.item);
}
</script>

<style scoped>
.chat-item {
  cursor: pointer;
  background: var(--wa-bg-1);
  padding: 12px 16px;
  gap: 14px;
  transition: background 0.12s ease;
}

.chat-item:hover {
  background: var(--wa-bg-2);
}

.chat-item.active {
  background: var(--wa-hover);
}

.chat-item-body {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
  border-bottom: 1px solid var(--wa-border-1);
  padding-bottom: 12px;
  align-self: stretch;
  justify-content: center;
}

.chat-item:last-child .chat-item-body {
  border-bottom: none;
}

.chat-item-top {
  gap: 8px;
}

.chat-item-name {
  color: var(--wa-text-1);
  font-size: 16px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
}

.chat-item-time {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--wa-text-2);
}

.chat-item-preview {
  display: flex;
  align-items: center;
  color: var(--wa-text-2);
  font-size: 13.5px;
  min-width: 0;
}

.chat-item-last {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.blur-sensitive {
  filter: blur(6px);
  transition: filter 0.3s ease-in-out;
}

.chat-item:hover .blur-sensitive {
  filter: none;
}
</style>
