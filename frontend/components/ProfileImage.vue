<template>
  <img :src="displayUrl" alt="Profile Photo" class="avatar-img rounded-circle"
       @error="onError" id="pic">
</template>

<script setup lang="ts">
import {useMainStore} from "~/store";

const store = useMainStore()
const props = defineProps(['id', 'urlProvided', 'cacheUrl'])
const key = ref(0)
const failed = ref(false)

const url = computed(() => {
  if (props.urlProvided) {
    return props.urlProvided
  } else if (props.id) {
    return useRuntimeConfig().public.api.getProfileImage.replace(":chatId", props.id.toString())
  } else {
    return '/default-avatar.svg'
  }
})

const cachedUrl = computed(() => {
  const cacheUrl = props.cacheUrl !== undefined ? props.cacheUrl.value : true
  if (cacheUrl) {
    return url.value + `?cache=${key.value}`
  }
  return url.value

})

const displayUrl = computed(() => (failed.value ? '/default-avatar.svg' : cachedUrl.value))

function onError() {
  failed.value = true
}

function forceUpdate() {
  key.value += 1
  failed.value = false
}

watch(
    () => store.reloadImageProfile,
    (reloadChatActive) => {
      if (reloadChatActive) {
        forceUpdate()
      }
    }
)

watch(() => props.id, () => {
  failed.value = false
})

</script>

<style scoped>
.avatar-img {
  width: 49px;
  height: 49px;
  object-fit: cover;
  flex-shrink: 0;
  background: #6a7175;
}
</style>
