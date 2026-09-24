<script setup lang="ts">
const props = defineProps({
  items: { type: Array, default: () => [] },
  initialIndex: { type: Number, default: 0 },
});

const emit = defineEmits(['close']);

const currentIndex = ref(Math.max(0, props.initialIndex));
const zoom = ref(1);
const translate = ref({ x: 0, y: 0 });

const current = computed(() => (props.items as any[])[currentIndex.value]);
const isImage = computed(() => current.value?.type === 'IMAGE');
const isVideo = computed(() => current.value?.type === 'VIDEO');

const zoomDisplay = computed(() => `${Math.round(zoom.value * 100)}%`);

function resetView() {
  zoom.value = 1;
  translate.value = { x: 0, y: 0 };
}

function goTo(index: number) {
  const list = props.items as any[];
  if (index < 0 || index >= list.length) return;
  currentIndex.value = index;
  resetView();
}

function prev() {
  goTo(currentIndex.value - 1);
}

function next() {
  goTo(currentIndex.value + 1);
}

function close() {
  resetView();
  emit('close');
}

function toggleZoom() {
  if (zoom.value > 1) {
    resetView();
  } else {
    zoom.value = 2.5;
  }
}

function zoomIn() {
  zoom.value = Math.min(8, zoom.value + 0.5);
}

function zoomOut() {
  zoom.value = Math.max(1, zoom.value - 0.5);
  if (zoom.value === 1) translate.value = { x: 0, y: 0 };
}

function onWheel(event: WheelEvent) {
  event.preventDefault();
  const step = event.deltaY < 0 ? 0.35 : -0.35;
  zoom.value = Math.min(8, Math.max(1, zoom.value + step));
  if (zoom.value === 1) translate.value = { x: 0, y: 0 };
}

// Drag to pan when zoomed in
const dragging = ref(false);
const didDrag = ref(false);
const dragStart = ref({ x: 0, y: 0 });
const originStart = ref({ x: 0, y: 0 });

function onPointerDown(event: PointerEvent) {
  if (zoom.value <= 1) return;
  dragging.value = true;
  didDrag.value = false;
  dragStart.value = { x: event.clientX, y: event.clientY };
  originStart.value = { ...translate.value };
  (event.currentTarget as HTMLElement).setPointerCapture(event.pointerId);
}

function onPointerMove(event: PointerEvent) {
  if (!dragging.value) return;
  const dx = event.clientX - dragStart.value.x;
  const dy = event.clientY - dragStart.value.y;
  if (Math.abs(dx) + Math.abs(dy) > 3) didDrag.value = true;
  translate.value = {
    x: originStart.value.x + dx,
    y: originStart.value.y + dy,
  };
}

function onPointerUp() {
  dragging.value = false;
}

function onStageClick() {
  if (!isImage.value) return;
  if (didDrag.value) {
    didDrag.value = false;
    return;
  }
  toggleZoom();
}

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') close();
  else if (event.key === 'ArrowLeft') prev();
  else if (event.key === 'ArrowRight') next();
  else if (event.key === '+') zoomIn();
  else if (event.key === '-') zoomOut();
}

onMounted(() => {
  window.addEventListener('keydown', onKeydown);
  document.body.style.overflow = 'hidden';
});

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown);
  document.body.style.overflow = '';
});
</script>

<template>
  <div class="media-viewer" @wheel="onWheel">
    <div class="mv-backdrop" @click.self="close"></div>

    <div class="mv-header">
      <span class="mv-counter">{{ currentIndex + 1 }} / {{ items.length }}</span>
      <div class="mv-actions">
        <a
            v-if="current"
            class="mv-icon"
            :href="current.url"
            :download="current.name"
            title="Baixar"
        >
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5M16.5 12 12 16.5m0 0L7.5 12m4.5 4.5V3"/>
          </svg>
        </a>
        <button type="button" class="mv-icon" title="Aproximar" @click="zoomIn">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607ZM10.5 7.5v6m3-3h-6"/>
          </svg>
        </button>
        <button type="button" class="mv-icon" title="Afastar" @click="zoomOut">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607ZM13.5 10.5h-6"/>
          </svg>
        </button>
        <button type="button" class="mv-icon" title="Fechar" @click="close">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>
    </div>

    <button v-if="currentIndex > 0" type="button" class="mv-nav mv-nav--prev" @click="prev" title="Anterior">
      <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5"/>
      </svg>
    </button>
    <button v-if="currentIndex < items.length - 1" type="button" class="mv-nav mv-nav--next" @click="next" title="Próximo">
      <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <path stroke-linecap="round" stroke-linejoin="round" d="m8.25 4.5 7.5 7.5-7.5 7.5"/>
      </svg>
    </button>

    <div
        class="mv-stage"
        @click="onStageClick"
        @pointerdown="onPointerDown"
        @pointermove="onPointerMove"
        @pointerup="onPointerUp"
        @pointercancel="onPointerUp"
    >
      <img
          v-if="isImage"
          class="mv-media"
          :src="current.url"
          :alt="current.name"
          :style="{ transform: `translate(${translate.x}px, ${translate.y}px) scale(${zoom})` }"
          draggable="false"
      />
      <video
          v-else-if="isVideo"
          class="mv-media mv-media--video"
          :src="current.url"
          controls
          autoplay
      ></video>
      <span v-if="isImage && zoom > 1" class="mv-zoom-label">{{ zoomDisplay }}</span>
    </div>

    <div v-if="items.length > 1" class="mv-thumbs">
      <button
          v-for="(item, i) in items"
          :key="item.url + i"
          type="button"
          class="mv-thumb"
          :class="{ 'mv-thumb--active': i === currentIndex }"
          @click="goTo(i)"
      >
        <img v-if="item.type === 'IMAGE'" :src="item.url" :alt="item.name" loading="lazy"/>
        <span v-else class="mv-thumb-video">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true">
            <path d="M8 5.14v13.72a1 1 0 0 0 1.53.85l11.2-6.86a1 1 0 0 0 0-1.7L9.53 4.29A1 1 0 0 0 8 5.14z"/>
          </svg>
        </span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.media-viewer {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  flex-direction: column;
  color: #e9edef;
}

.mv-backdrop {
  position: absolute;
  inset: 0;
  background: rgba(11, 20, 26, 0.92);
  backdrop-filter: blur(4px);
}

.mv-header {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
}

.mv-counter {
  font-size: 13px;
  color: var(--wa-text-3);
}

.mv-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.mv-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--wa-text-3);
  cursor: pointer;
  transition: background 0.15s ease;
}

.mv-icon:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #e9edef;
}

.mv-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 3;
  width: 48px;
  height: 48px;
  border: none;
  border-radius: 50%;
  background: rgba(11, 20, 26, 0.5);
  color: #e9edef;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mv-nav:hover {
  background: rgba(11, 20, 26, 0.8);
}

.mv-nav--prev {
  left: 16px;
}

.mv-nav--next {
  right: 16px;
}

.mv-stage {
  position: relative;
  z-index: 1;
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: zoom-in;
  touch-action: none;
}

.mv-media {
  max-width: 92%;
  max-height: 88%;
  object-fit: contain;
  user-select: none;
  transition: transform 0.12s ease-out;
}

.mv-media--video {
  max-width: 90%;
  max-height: 82%;
}

.mv-zoom-label {
  position: absolute;
  bottom: 14px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(11, 20, 26, 0.7);
  padding: 4px 12px;
  border-radius: 14px;
  font-size: 12px;
}

.mv-thumbs {
  position: relative;
  z-index: 2;
  display: flex;
  gap: 8px;
  padding: 10px 16px 14px;
  overflow-x: auto;
  justify-content: center;
}

.mv-thumb {
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 6px;
  overflow: hidden;
  background: var(--wa-bg-3);
  cursor: pointer;
}

.mv-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.mv-thumb--active {
  border-color: var(--wa-green-1);
}

.mv-thumb-video {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: var(--wa-text-3);
}
</style>
