<template>
  <div class="message-row" :class="rowClasses">
    <div class="message-bubble" :class="bubbleClasses">
      <div v-if="showAuthor" class="message-author" :style="{ color: authorColor }">{{ message.author }}</div>
      <div
          v-if="!isAudio && !isVisualMedia"
          class="message-content"
          v-html="formattedContent"
          :class="{ 'blur-sensitive': store.blurEnabled }"
      ></div>
      <voice-message
          v-if="isAudio"
          :attachment="message.attachment"
          :outgoing="isAuthorSelf"
          :class="{ 'blur-sensitive': store.blurEnabled }"
      />
      <focusable-attachment
          v-else-if="hasAttachment"
          :attachment="message.attachment"
          :class="{ 'blur-sensitive': store.blurEnabled }"
      />
      <div v-if="!isAudio" class="message-meta" :class="{ 'message-meta--overlay': isVisualMedia }">
        <span class="message-time">{{ timeText }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useMainStore } from '~/store';

const store = useMainStore();

const props = defineProps(['message']);

const formattedContent = computed(() => {
  const escaped = escapeHtml(props.message.content ?? '');
  return escaped.replace(
      /https?:\/\/[^\s]+/g,
      '<a href="$&" target="_blank" rel="noopener noreferrer">$&</a>'
  );
});

const timeText = computed(() => {
  const date = new Date(props.message.createdAt);
  const hh = String(date.getHours()).padStart(2, '0');
  const mm = String(date.getMinutes()).padStart(2, '0');
  return `${hh}:${mm}`;
});

function escapeHtml(text: string): string {
  return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
}

const hasAttachment = computed(() => Boolean(props.message.attachment));

const isAudio = computed(() => props.message.attachment?.type === 'AUDIO');

const isVisualMedia = computed(() => {
  const type = props.message.attachment?.type;
  return type === 'IMAGE' || type === 'VIDEO';
});

const isSystemMessage = computed(() => props.message.authorType === 'SYSTEM');

const isAuthorSelf = computed(() => props.message.author === store.authorActive);

const showAuthor = computed(() => !isSystemMessage.value && !isAuthorSelf.value);

const rowClasses = computed(() => ({
  'row-out': isAuthorSelf.value && !isSystemMessage.value,
  'row-in': !isAuthorSelf.value && !isSystemMessage.value,
  'row-system': isSystemMessage.value,
}));

const bubbleClasses = computed(() => ({
  'bubble-out': isAuthorSelf.value && !isSystemMessage.value,
  'bubble-in': !isAuthorSelf.value && !isSystemMessage.value,
  'bubble-system': isSystemMessage.value,
  'bubble-media': isVisualMedia.value,
}));

const authorColor = computed(() => colorForAuthor(props.message.author));

const AUTHOR_COLORS = ['#00a884', '#53bdeb', '#766ac8', '#dfa521', '#e0585c', '#f36bd1'];

function colorForAuthor(author: string): string {
  let hash = 0;
  for (let i = 0; i < author.length; i++) {
    hash = (hash * 31 + author.charCodeAt(i)) >>> 0;
  }
  return AUTHOR_COLORS[hash % AUTHOR_COLORS.length];
}
</script>

<style scoped>
.message-row {
  display: flex;
  width: 100%;
  margin: 2px 0;
}

.row-out {
  justify-content: flex-end;
}

.row-in {
  justify-content: flex-start;
}

.row-system {
  justify-content: center;
}

.message-bubble {
  position: relative;
  max-width: 65%;
  padding: 6px 8px 8px 10px;
  border-radius: 7.5px;
  box-shadow: 0 1px 0.5px rgba(11, 20, 26, 0.13);
}

.bubble-media {
  padding: 3px;
  background: transparent;
}

.bubble-media.bubble-out {
  background: transparent;
}

.bubble-media.bubble-in {
  background: transparent;
}

.bubble-out {
  background: var(--wa-outgoing);
}

.bubble-in {
  background: var(--wa-incoming);
}

.bubble-system {
  background: var(--wa-bg-3);
  border-radius: 7.5px;
  max-width: 85%;
  text-align: center;
  box-shadow: none;
}

.message-author {
  font-size: 12.5px;
  font-weight: 600;
  margin-bottom: 2px;
}

.message-content {
  color: var(--wa-text-1);
  font-size: 14.2px;
  line-height: 19px;
  overflow-wrap: break-word;
  word-break: break-word;
  white-space: pre-wrap;
}

.message-content :deep(a) {
  color: var(--wa-text-5);
  text-decoration: underline;
}

.bubble-system .message-content {
  color: var(--wa-text-2);
  font-size: 12.5px;
  line-height: 21px;
  text-align: center;
}

.message-meta {
  display: flex;
  justify-content: flex-end;
  margin-top: 2px;
}

.message-meta--overlay {
  position: absolute;
  right: 8px;
  bottom: 6px;
  margin-top: 0;
}

.message-meta--overlay .message-time {
  background: rgba(11, 20, 26, 0.55);
  color: #e9edef;
  padding: 2px 7px;
  border-radius: 12px;
}

.message-time {
  font-size: 11px;
  color: var(--wa-text-5);
  white-space: nowrap;
}

.bubble-system .message-time {
  color: var(--wa-text-2);
}

.blur-sensitive {
  filter: blur(6px);
  transition: filter 0.3s ease-in-out;
}

.message-bubble:hover .blur-sensitive {
  filter: none;
}
</style>
