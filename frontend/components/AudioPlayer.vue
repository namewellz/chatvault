<template>
  <div class="flex items-center gap-2.5 py-1 select-none" @click.stop>
    <!-- Play/Pause button -->
    <button
      class="flex-shrink-0 w-9 h-9 rounded-full flex items-center justify-center transition-colors hover:brightness-110"
      style="background: rgba(255,255,255,0.2)"
      @click.stop="togglePlay"
    >
      <svg
        v-if="!isPlaying"
        fill="currentColor"
        viewBox="0 0 24 24"
        class="w-4 h-4 text-white"
        style="margin-left: 2px"
      >
        <path d="M8 5v14l11-7z" />
      </svg>
      <svg
        v-else
        fill="currentColor"
        viewBox="0 0 24 24"
        class="w-4 h-4 text-white"
      >
        <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z" />
      </svg>
    </button>

    <!-- Waveform + duration -->
    <div class="flex-1 min-w-0 flex flex-col gap-1">
      <!-- Waveform bars -->
      <div
        ref="waveformEl"
        class="flex items-center gap-px cursor-pointer"
        style="height: 32px"
        @click.stop="seekTo"
      >
        <div
          v-for="(h, i) in bars"
          :key="i"
          class="rounded-full transition-colors duration-75"
          :style="{
            flex: '1',
            height: h + '%',
            backgroundColor: i <= playedIndex ? '#53bdeb' : 'rgba(255,255,255,0.35)',
          }"
        />
      </div>

      <!-- Duration / current time -->
      <span style="font-size: 11px; color: rgba(255,255,255,0.65); line-height: 1">
        {{ timeDisplay }}
      </span>
    </div>

    <!-- Hidden audio element -->
    <audio
      ref="audioEl"
      :src="props.src"
      preload="metadata"
      @timeupdate="onTimeUpdate"
      @ended="onEnded"
      @loadedmetadata="onMeta"
    />
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{ src: string }>()

const audioEl = ref<HTMLAudioElement>()
const waveformEl = ref<HTMLElement>()
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)

const BAR_COUNT = 45

function seededRng(n: number): number {
  const x = Math.sin(n) * 10000
  return x - Math.floor(x)
}

const bars = computed(() => {
  const seed = props.src.split('').reduce((acc, c, i) => acc + c.charCodeAt(0) * (i + 1), 0)
  return Array.from({ length: BAR_COUNT }, (_, i) => {
    const r = seededRng(seed + i * 3)
    const r2 = seededRng(seed + i * 7 + 13)
    const height = r < 0.3 ? 12 + r2 * 25 : 38 + r2 * 55
    return Math.min(95, Math.max(12, Math.round(height)))
  })
})

const playedIndex = computed(() => {
  if (!duration.value || currentTime.value <= 0) return -1
  return Math.ceil((currentTime.value / duration.value) * BAR_COUNT) - 1
})

const timeDisplay = computed(() => {
  const t = currentTime.value > 0 ? currentTime.value : duration.value
  const m = Math.floor(t / 60)
  const s = Math.floor(t % 60)
  return `${m}:${String(s).padStart(2, '0')}`
})

function togglePlay() {
  if (!audioEl.value) return
  if (isPlaying.value) {
    audioEl.value.pause()
    isPlaying.value = false
  } else {
    audioEl.value.play()
    isPlaying.value = true
  }
}

function onTimeUpdate() {
  currentTime.value = audioEl.value?.currentTime ?? 0
}

function onMeta() {
  duration.value = audioEl.value?.duration ?? 0
}

function onEnded() {
  isPlaying.value = false
  currentTime.value = 0
  if (audioEl.value) audioEl.value.currentTime = 0
}

function seekTo(e: MouseEvent) {
  if (!audioEl.value || !waveformEl.value || !duration.value) return
  const rect = waveformEl.value.getBoundingClientRect()
  const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  audioEl.value.currentTime = ratio * duration.value
  currentTime.value = audioEl.value.currentTime
}

onUnmounted(() => audioEl.value?.pause())
</script>
