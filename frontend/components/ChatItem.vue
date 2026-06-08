<template>
  <div
    class="flex items-center px-4 py-2 border-b border-wa-border cursor-pointer min-h-[73px] transition-colors blur-parent"
    :class="isActive ? 'bg-wa-hover' : 'hover:bg-wa-hover/60'"
    @click="emitThisChatActive"
  >
    <!-- Avatar -->
    <div class="flex-shrink-0 mr-3">
      <profile-image
        :id="item.chatId"
        :class="{ 'blur-sensitive': store.blurEnabled }"
        class="w-12 h-12 rounded-full object-cover"
      />
    </div>

    <!-- Content -->
    <div class="flex-1 min-w-0">
      <!-- Name row -->
      <div class="flex items-center justify-between gap-2">
        <span
          class="text-wa-text font-semibold text-[15px] truncate"
          :class="{ 'blur-sensitive': store.blurEnabled }"
        >
          {{ item.chatName }}
        </span>
        <message-created-at
          :date="item.msgCreatedAt"
          class="text-wa-text-muted text-[12px] flex-shrink-0"
          :class="{ 'blur-sensitive': store.blurEnabled }"
        />
      </div>

      <!-- Preview row -->
      <div
        class="text-wa-text-muted text-[13px] truncate mt-0.5"
        :class="{ 'blur-sensitive': store.blurEnabled }"
      >
        {{ item.content }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useMainStore } from '~/store'

const store = useMainStore()
const props = defineProps(['item'])
const emit = defineEmits(['update:chat-active'])

const isActive = computed(() => store.chatActive?.chatId === props.item?.chatId)

function emitThisChatActive() {
  emit('update:chat-active', props.item)
}
</script>
