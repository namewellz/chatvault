<script setup lang="ts">
import { useMainStore } from "~/store";

const props = defineProps(['attachment'])

const store = useMainStore();

const isAudio = computed(() => props.attachment?.type === 'AUDIO')
const isVisual = computed(() => props.attachment?.type === 'IMAGE' || props.attachment?.type === 'VIDEO')

const mediaItems = computed(() =>
    store.attachments.filter((item: any) => item.type === 'IMAGE' || item.type === 'VIDEO')
)

const showViewer = ref(false)
const viewerIndex = ref(0)

function openViewer() {
  const idx = mediaItems.value.findIndex((item: any) => item.url === props.attachment.url)
  viewerIndex.value = idx >= 0 ? idx : 0
  showViewer.value = true
}
</script>

<template>

  <voice-message v-if="isAudio" :attachment="props.attachment" />

  <lazy-attachment
      v-else
      :attachment="props.attachment"
      :role="isVisual ? 'button' : undefined"
      @click="isVisual && openViewer()"
  />

  <media-viewer
      v-if="showViewer"
      :items="mediaItems"
      :initial-index="viewerIndex"
      @close="showViewer = false"
  />

</template>

<style scoped>

</style>