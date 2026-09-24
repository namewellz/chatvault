<template>
  <div class="search-bar">
    <svg class="search-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
      <path stroke-linecap="round" stroke-linejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607z"/>
    </svg>
    <input
        type="text"
        class="form-control"
        placeholder="Search..."
        v-model="store.searchQuery"
        @input="onSearch"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import {useMainStore} from "~/store";
const store = useMainStore()
const props = defineProps({
  chatId: { type: [Number, null], default: null },
});
const emit = defineEmits(["search"]);

const searchQuery = ref("");

function onSearch() {
  store.clearMessages();
  emit("search", { query: searchQuery.value, chatId: props.chatId });
}

watch(() => props.chatId, () => (searchQuery.value = ""));
</script>

<style scoped>
.search-bar {
  position: relative;
  width: 100%;
  max-width: 340px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--wa-text-2);
  pointer-events: none;
}

.search-bar .form-control {
  padding-left: 42px;
  border-radius: 8px;
  border: none;
  background: var(--wa-bg-2);
  height: 35px;
}
</style>
