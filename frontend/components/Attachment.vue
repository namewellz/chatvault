<template>
  <div class="attachment">
    <img class="wa-image" loading="lazy" v-if="isImage" :src="attachment.url" :alt="attachment.name"/>
    <video class="wa-video" v-else-if="isVideo" :src="attachment.url" controls></video>
    <voice-message v-else-if="isAudio" :attachment="attachment" />
    <a class="w-100" v-else :download='attachment.name'
       :href="attachment.url">{{ attachment.name }}</a>
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
const props = defineProps(['attachment'])

const isImage = computed(() => props.attachment.type === 'IMAGE')
const isVideo = computed(() => props.attachment.type === 'VIDEO')
const isAudio = computed(() => props.attachment.type === 'AUDIO')
const isPDF = computed(() => props.attachment.type === 'PDF')
</script>
<style scoped>
.wa-image,
.wa-video {
  display: block;
  max-width: 330px;
  max-height: 330px;
  width: auto;
  height: auto;
  border-radius: 7.5px;
}

.wa-video {
  background: #000;
}
</style>