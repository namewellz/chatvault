<template>
  <!-- System message: centered pill -->
  <div v-if="isSystemMessage" class="wa-message-row flex justify-center my-1">
    <div class="wa-bubble-system" :class="{ 'blur-sensitive': store.blurEnabled }">
      {{ message.content }}
    </div>
  </div>

  <!-- Regular message: bubble left or right -->
  <div
    v-else
    class="wa-message-row flex mb-1"
    :class="[
      isAuthorSelf ? 'justify-end' : 'justify-start',
      props.enableJump ? 'cursor-pointer' : '',
    ]"
    :title="enableJump ? 'Clique para abrir contexto' : undefined"
    @click="handleClick"
  >
    <div
      class="max-w-[65%] px-2.5 py-1.5 text-wa-text relative"
      :class="[
        isAuthorSelf ? 'wa-bubble-sent mr-2' : 'wa-bubble-recv ml-2',
        isHighlighted ? 'wa-message-highlight' : '',
        isAudioAttachment ? 'min-w-[240px]' : 'min-w-[80px]',
      ]"
    >
      <!-- Author name (only for received, when there are multiple people) -->
      <div
        v-if="!isAuthorSelf && message.author"
        class="text-[12.8px] font-semibold text-wa-green mb-0.5 leading-tight"
        :class="{ 'blur-sensitive': store.blurEnabled }"
      >
        {{ message.author }}
      </div>

      <!-- Audio attachment: custom waveform player -->
      <audio-player
        v-if="isAudioAttachment"
        :src="message.attachment.url"
        :class="{ 'blur-sensitive': store.blurEnabled }"
        class="mb-1"
      />
      <!-- Other attachments: focusable with fullscreen modal -->
      <focusable-attachment
        v-else-if="hasAttachment"
        :attachment="message.attachment"
        :class="{ 'blur-sensitive': store.blurEnabled }"
        class="mb-1"
      />

      <!-- Message text (hidden for audio-only – content is just the filename) -->
      <div
        v-if="!isAudioAttachment"
        class="text-[14.2px] leading-[1.4] whitespace-pre-wrap break-words"
        v-html="formattedContent"
        :class="{ 'blur-sensitive': store.blurEnabled }"
      />

      <!-- Footer: timestamp -->
      <div class="flex justify-end items-center gap-1 mt-0.5 -mb-0.5">
        <span class="text-[11px] text-wa-text-muted/80 leading-none">{{ formattedDate }}</span>
      </div>

      <!-- Jump hint -->
      <div
        v-if="enableJump"
        class="absolute -top-5 left-0 text-[10px] text-wa-green bg-wa-header px-2 py-0.5 rounded-full opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity"
      >
        contexto
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useMainStore } from '~/store'
import { useDateFormatting } from '~/composables/useDateFormatting'

const store = useMainStore()
const { formatDate } = useDateFormatting()

const props = defineProps({
  message: Object,
  highlightUntilDate: [String, null],
  highlightMessageId: [Number, null],
  enableJump: { type: Boolean, default: false },
})
const emit = defineEmits(['jump'])

const isHighlightedByDate = ref(false)
const isHighlightedByMessage = ref(false)
const isHighlighted = computed(() => isHighlightedByDate.value || isHighlightedByMessage.value)

const formattedContent = computed(() =>
  props.message?.content?.replace(
    /https?:\/\/[^\s]+/g,
    (url: string) => `<a href="${url}" target="_blank" rel="noopener noreferrer" class="text-wa-link underline">${url}</a>`
  ) ?? ''
)

const formattedDate = computed(() => formatDate(props.message?.createdAt))
const hasAttachment = computed(() => Boolean(props.message?.attachment))
const isAudioAttachment = computed(() => props.message?.attachment?.type === 'AUDIO')
const isSystemMessage = computed(() => props.message?.authorType === 'SYSTEM')
const isAuthorSelf = computed(() => props.message?.author === store.authorActive)

watch(
  () => props.highlightUntilDate,
  (newDate) => {
    if (newDate && props.message) {
      const msgDate = new Date(props.message.createdAt).toDateString()
      const highlightDate = new Date(newDate).toDateString()
      if (msgDate === highlightDate) {
        isHighlightedByDate.value = true
        setTimeout(() => {
          isHighlightedByDate.value = false
          store.clearHighlight()
        }, 3000)
      }
    }
  },
  { immediate: true }
)

watch(
  () => props.highlightMessageId,
  (newId) => {
    if (!newId || !props.message) return
    if (props.message.id === newId) {
      isHighlightedByMessage.value = true
      setTimeout(() => {
        isHighlightedByMessage.value = false
        store.clearHighlightMessage()
      }, 3000)
    }
  },
  { immediate: true }
)

function handleClick() {
  if (!props.enableJump || !props.message) return
  emit('jump', { id: props.message.id, createdAt: props.message.createdAt })
}
</script>

<style scoped>
/* Link style inside message content */
:deep(a) {
  color: #53bdeb;
  text-decoration: underline;
}

:deep(a:hover) {
  color: #93d5f0;
}
</style>
