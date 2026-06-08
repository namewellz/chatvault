<template>
  <div class="wa-attachment rounded-xl border border-wa-border bg-wa-search p-2 transition-colors hover:border-wa-green/40">
    <img
      class="rounded-lg w-full max-h-[300px] object-contain"
      loading="lazy"
      v-if="isImage"
      :src="attachment.url"
      :alt="attachment.name"
    />
    <video class="rounded-lg w-full" v-else-if="isVideo" :src="attachment.url" controls />
    <audio class="w-full mt-1" v-else-if="isAudio" :src="attachment.url" controls />
    <a
      v-else
      class="flex flex-col gap-1 p-3 rounded-lg bg-wa-hover/50 hover:bg-wa-hover transition-colors no-underline"
      :download="attachment.name"
      :href="attachment.url"
    >
      <span class="text-wa-text text-sm font-semibold break-all">{{ attachment.name }}</span>
      <span class="text-wa-green text-xs uppercase tracking-wide">{{ t('download') }}</span>
    </a>
    <slot />
  </div>
</template>

<script setup lang="ts">
import { useUiText } from '~/composables/useUiText'

const props = defineProps(['attachment'])
const { t } = useUiText()

const isImage = computed(() => props.attachment.type === 'IMAGE')
const isVideo = computed(() => props.attachment.type === 'VIDEO')
const isAudio = computed(() => props.attachment.type === 'AUDIO')
</script>
