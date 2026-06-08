<script setup lang="ts">
import { useMainStore } from '~/store'
import { useUiText } from '~/composables/useUiText'

const store = useMainStore()
const { t } = useUiText()

const galleryOptions = computed(() => [
  { type: 'ALL', label: t('filterAll') },
  { type: 'VIDEO', label: t('filterVideo') },
  { type: 'IMAGE', label: t('filterImage') },
  { type: 'PDF', label: t('filterDocuments') },
  { type: 'AUDIO', label: t('filterAudio') },
])

const galleryFileType = ref('ALL')

const attachments = computed(() =>
  galleryFileType.value === 'ALL'
    ? store.attachments
    : store.attachments.filter((item) => item.type === galleryFileType.value)
)

function setGalleryFilter(type: string) {
  galleryFileType.value = type
}
</script>

<template>
  <div class="text-wa-text">
    <slot />

    <!-- Header -->
    <div class="flex items-center justify-between gap-3 mb-3 px-4 pt-3">
      <span class="font-semibold text-[15px]">{{ t('galleryTitle') }}</span>
      <div class="flex flex-wrap gap-1.5">
        <button
          v-for="item in galleryOptions"
          :key="item.type"
          class="px-3 py-1 rounded-full text-[13px] border transition-colors"
          :class="
            galleryFileType === item.type
              ? 'bg-wa-green/20 border-wa-green/50 text-wa-green font-semibold'
              : 'border-wa-border text-wa-text-muted hover:bg-wa-hover'
          "
          @click="setGalleryFilter(item.type)"
        >
          {{ item.label }}
        </button>
      </div>
    </div>

    <!-- Grid -->
    <div class="grid grid-cols-2 sm:grid-cols-3 gap-2 px-3 pb-4">
      <div
        v-for="item in attachments"
        :key="item.url"
        v-memo="[galleryFileType]"
        class="rounded-xl border border-wa-border bg-wa-search overflow-hidden"
      >
        <focusable-attachment
          :attachment="item"
          :class="{ 'blur-sensitive': store.blurEnabled }"
          class="blur-parent"
        />
      </div>
    </div>
  </div>
</template>
