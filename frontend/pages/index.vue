<template>
  <div class="index-page" ref="indexRef">

    <div class="loading-overlay" v-if="store.loading">
      <div class="spinner-border" role="status" aria-hidden="true"></div>
    </div>

    <main class="container-fluid app-shell">
      <new-chat-uploader v-if="createChatAction"
                         @update:chats="refreshPage"
                         @exit:dialog="() => createChatAction = false"
      />

      <chat-exporter v-else-if="exportChatAction"
                     allow-download-all="true"
                     @exit:dialog="() => exportChatAction = false"/>

      <template v-else>
        <chat-list :chats="chats"
                   :mobile="isMobile"
                   @create:chat="createNewChat"
                   @update:chat-active="updateChatActive"
                   @export:chat="exportChat"
                   @update:disk-import="refreshPage"
        />
        <message-area
            :mobile="isMobile"
        />
        <chat-config v-if="store.chatConfigOpen"
                     @refresh:page="() => refresh()"
        />
      </template>
    </main>

  </div>
</template>

<script setup lang="ts">
import ChatList from "~/components/ChatList.vue";
import MessageArea from "~/components/MessageArea.vue";
import ChatConfig from "~/components/ChatConfig.vue";
import {useMainStore} from "~/store";

const store = useMainStore()
const listChatsAPIUrl = useRuntimeConfig().public.api.listChats
const getAppVersionAPIUrl = useRuntimeConfig().public.api.appVersion
const {data: chats, refresh} = await useFetch(listChatsAPIUrl)
const {data: versionData} = await useFetch(getAppVersionAPIUrl)
const appVersion = computed(() => versionData.value?.version ?? '')
const isMobile = ref(true)
const indexRef = ref(null)
const createChatAction = ref(false)
const exportChatAction = ref(false)

function checkWindowSize() {
  if (indexRef.value) {
    isMobile.value = indexRef.value.offsetWidth <= 575;
  }

}

function refreshPage() {
  store.loading = true
  createChatAction.value = false
  exportChatAction.value = false
  refresh()
  store.loading = false
}

function createNewChat() {
  createChatAction.value = true
}

function exportChat() {
  exportChatAction.value = true
}

function updateChatActive(item: any) {
  store.openChat(item)
}

watchEffect(() => {
  if (indexRef.value) {
    indexRef.value.addEventListener('resize', checkWindowSize);
    checkWindowSize()
  }
})

onBeforeMount(() => {
  window.addEventListener("resize", checkWindowSize);
})

onMounted(() => {
  nextTick(() => {
    if (indexRef.value) {
      console.log("add event listener")
      indexRef.value.addEventListener('resize', checkWindowSize);
      checkWindowSize()
    }
  })
})


</script>


<style>
.index-page {
  font-size: 15px;
  color: var(--wa-text-1);
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.app-shell {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  width: 100%;
  height: 100%;
  padding: 0;
  margin: 0;
}

.loading-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(11, 20, 26, 0.6);
  backdrop-filter: blur(2px);
}

.loading-overlay .spinner-border {
  color: var(--wa-green-1);
  width: 3rem;
  height: 3rem;
}
</style>
