<script setup lang="ts">
import { useUiText } from '~/composables/useUiText'

const props = defineProps(['attachment'])
const { t } = useUiText()

const showMedia = ref(false)

function toggleModal() { showMedia.value = !showMedia.value }

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && showMedia.value) showMedia.value = false
}

onMounted(() => window.addEventListener('keydown', handleKeydown))
onBeforeUnmount(() => window.removeEventListener('keydown', handleKeydown))
</script>

<template>
  <lazy-attachment :attachment="props.attachment" @click="toggleModal" role="button">
    <!-- Fullscreen modal -->
    <teleport to="body">
      <div
        v-if="showMedia"
        class="fixed inset-0 z-[1050] flex items-center justify-center bg-black/80 backdrop-blur-sm"
        @click.self="toggleModal"
      >
        <div class="relative bg-wa-header rounded-2xl shadow-2xl p-4 max-w-[min(860px,90vw)] w-full max-h-[90vh] flex flex-col items-center border border-wa-border">
          <!-- Close button -->
          <button
            class="absolute top-3 right-3 p-1.5 rounded-full bg-wa-hover text-wa-icon hover:text-wa-text transition-colors z-10"
            :aria-label="t('close')"
            @click="toggleModal"
          >
            <svg viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>

          <!-- Media -->
          <div class="max-h-[75vh] w-full overflow-auto flex justify-center">
            <lazy-attachment :attachment="props.attachment" />
          </div>

          <p class="text-wa-text-muted text-xs mt-3">{{ t('searchHintClose') }}</p>
        </div>
      </div>
    </teleport>
  </lazy-attachment>
</template>
