<template>
  <img
    :src="currentSrc"
    :alt="t('profilePhotoAlt')"
    class="rounded-full object-cover flex-shrink-0"
    style="width:49px;height:49px;"
    @error="onError"
  />
</template>

<script setup lang="ts">
import { useMainStore } from '~/store'
import { useUiText } from '~/composables/useUiText'

const store = useMainStore()
const { t } = useUiText()
const props = defineProps(['id', 'urlProvided', 'cacheUrl'])
const key = ref(0)
const errored = ref(false)

// Generic person SVG as data URI (no external service needed)
const FALLBACK_SVG = `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 49 49'%3E%3Ccircle cx='24.5' cy='24.5' r='24.5' fill='%23202C33'/%3E%3Ccircle cx='24.5' cy='18' r='9.5' fill='%238696A0'/%3E%3Cellipse cx='24.5' cy='44' rx='17' ry='13' fill='%238696A0'/%3E%3C/svg%3E`

const apiUrl = computed(() => {
  if (props.urlProvided) return props.urlProvided
  if (props.id) return useRuntimeConfig().public.api.getProfileImage.replace(':chatId', props.id.toString())
  return null
})

const currentSrc = computed(() => {
  if (errored.value || !apiUrl.value) return FALLBACK_SVG
  const useCache = props.cacheUrl !== undefined ? props.cacheUrl.value : true
  return useCache ? apiUrl.value + `?cache=${key.value}` : apiUrl.value
})

function onError() {
  errored.value = true
}

watch(() => store.reloadImageProfile, (reload) => {
  if (reload) {
    errored.value = false
    key.value += 1
  }
})

watch(() => props.id, () => {
  errored.value = false
})
</script>
