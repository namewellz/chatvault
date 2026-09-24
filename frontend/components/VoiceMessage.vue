<template>
  <div class="voice-message" :class="{ 'voice-message--out': outgoing }" :style="{ width: voiceWidth }">
    <button type="button" class="voice-play" @click.stop="togglePlay" :aria-label="playing ? 'Pausar' : 'Reproduzir'">
      <svg v-if="!playing" viewBox="0 0 24 24" width="16" height="16" fill="currentColor" aria-hidden="true">
        <path d="M8 5.14v13.72a1 1 0 0 0 1.53.85l11.2-6.86a1 1 0 0 0 0-1.7L9.53 4.29A1 1 0 0 0 8 5.14z"/>
      </svg>
      <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="currentColor" aria-hidden="true">
        <rect x="6" y="5" width="4" height="14" rx="1"/>
        <rect x="14" y="5" width="4" height="14" rx="1"/>
      </svg>
    </button>

    <div class="voice-body">
      <div class="voice-wave">
        <span
            v-for="(h, i) in bars"
            :key="i"
            class="voice-bar"
            :class="{ 'is-played': i <= playedBars }"
            :style="{ height: h + '%' }"
        ></span>
      </div>
      <span class="voice-time">{{ displayTime }}</span>
    </div>

    <audio
        ref="audioRef"
        :src="playableSrc"
        preload="metadata"
        @loadedmetadata="onLoadedMetadata"
        @timeupdate="onTimeUpdate"
        @ended="onEnded"
        @play="playing = true"
        @pause="playing = false"
    ></audio>
  </div>
</template>

<script setup lang="ts">
const props = defineProps(['attachment', 'outgoing']);

const audioRef = ref<HTMLAudioElement | null>(null);
const playing = ref(false);
const duration = ref(0);
const currentTime = ref(0);

const bars = computed(() => generateBars(props.attachment?.name ?? ''));

const playableSrc = computed(() => {
  const url = props.attachment?.url ?? '';
  const sep = url.includes('?') ? '&' : '?';
  return `${url}${sep}format=mp3`;
});

const playedBars = computed(() => {
  if (duration.value <= 0) return 0;
  return Math.floor((currentTime.value / duration.value) * bars.value.length);
});

const displayTime = computed(() => {
  const time = playing.value || currentTime.value > 0 ? currentTime.value : duration.value;
  return formatTime(time);
});

const voiceWidth = computed(() => {
  if (duration.value > 0) {
    const px = 48 + duration.value * 8;
    return `${Math.min(340, Math.max(160, px))}px`;
  }
  return '220px';
});

function togglePlay() {
  const audio = audioRef.value;
  if (!audio) return;
  if (audio.paused) {
    audio.play().catch(() => {});
  } else {
    audio.pause();
  }
}

function onLoadedMetadata() {
  duration.value = audioRef.value?.duration || 0;
}

function onTimeUpdate() {
  currentTime.value = audioRef.value?.currentTime || 0;
}

function onEnded() {
  playing.value = false;
  currentTime.value = 0;
}

function formatTime(seconds: number): string {
  if (!seconds || Number.isNaN(seconds)) return '0:00';
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60);
  return `${m}:${String(s).padStart(2, '0')}`;
}

function generateBars(seed: string): number[] {
  let hash = 0;
  for (let i = 0; i < seed.length; i++) {
    hash = (hash * 31 + seed.charCodeAt(i)) >>> 0;
  }
  if (hash === 0) hash = 2166136261;

  const result: number[] = [];
  for (let i = 0; i < 30; i++) {
    hash = (hash * 1103515245 + 12345) >>> 0;
    result.push(22 + (hash % 78));
  }
  return result;
}
</script>

<style scoped>
.voice-message {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px 4px 4px;
  min-width: 160px;
  max-width: 340px;
  cursor: pointer;
}

.voice-play {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: var(--wa-hover);
  color: var(--wa-text-1);
  padding: 0;
}

.voice-message--out .voice-play {
  background: var(--wa-green-1);
  color: #ffffff;
}

.voice-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.voice-wave {
  display: flex;
  align-items: center;
  gap: 2px;
  height: 28px;
  overflow: hidden;
}

.voice-bar {
  flex-shrink: 0;
  width: 3px;
  border-radius: 2px;
  background: var(--wa-text-3);
  opacity: 0.45;
}

.voice-bar.is-played {
  opacity: 1;
  background: var(--wa-text-1);
}

.voice-message--out .voice-bar {
  background: #e9edef;
  opacity: 0.4;
}

.voice-message--out .voice-bar.is-played {
  opacity: 1;
  background: #e9edef;
}

.voice-time {
  font-size: 11px;
  line-height: 1;
  color: var(--wa-text-5);
  white-space: nowrap;
}

.voice-message--out .voice-time {
  color: #cfe9e4;
}
</style>
