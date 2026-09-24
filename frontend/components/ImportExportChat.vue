<script setup lang="ts" xmlns="http://www.w3.org/1999/html">
import {useMainStore} from "~/store";
const props = defineProps(['allowDownloadAll'])
const store = useMainStore()
const clickModal = ref(false)
const chatImportRef = ref(null)
const errorMessage = ref(undefined)
const disableUpload = ref(true)
const uploadProgress = ref<number | null>(null)

const modalClass = computed(() => {
  return {
    'fade show d-block': !!clickModal.value
  }
})

const importChatPath = computed(() => useRuntimeConfig().public.api.importChatById.replace(":chatId", store.chatActive?.chatId?.toString()))

const linkDownload = computed(() => {
  if (props.allowDownloadAll) {
    return useRuntimeConfig().public.api.exportAllChats
  } else {
    return useRuntimeConfig().public.api.exportChatById.replace(":chatId", store.chatActive?.chatId?.toString())
  }
})

const chatName = computed(() => {
  if (props.allowDownloadAll) {
    return "all-chats.zip"
  } else {
    return store.chatActive.chatName + '.zip'
  }
})

const uploadStatusLabel = computed(() => {
  if (uploadProgress.value === null) return null
  if (uploadProgress.value < 100) return `Enviando... ${uploadProgress.value}%`
  return 'Processando arquivo...'
})

function toggleModal() {
  clickModal.value = !clickModal.value
  errorMessage.value = undefined
  if (chatImportRef.value) {
    chatImportRef.value.value = ''
  }
}

async function onFilePicked() {
  if (chatImportRef?.value?.files && chatImportRef?.value?.files[0]) {
    disableUpload.value = false
  }
}

async function uploadFile() {
  if (!chatImportRef?.value?.files || !chatImportRef?.value?.files[0]) return

  store.loading = true
  errorMessage.value = undefined
  uploadProgress.value = 0
  disableUpload.value = true

  const form = new FormData()
  form.append("file", chatImportRef.value.files[0])

  await new Promise<void>((resolve, reject) => {
    const xhr = new XMLHttpRequest()

    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) {
        uploadProgress.value = Math.min(99, Math.round((event.loaded / event.total) * 100))
      }
    }

    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        uploadProgress.value = 100
        resolve()
      } else {
        try {
          reject({ data: JSON.parse(xhr.responseText) })
        } catch {
          reject({ message: `Erro HTTP ${xhr.status}` })
        }
      }
    }

    xhr.onerror = () => reject({ message: 'Falha de conexão' })
    xhr.onabort = () => reject({ message: 'Upload cancelado' })

    xhr.open('POST', importChatPath.value)
    xhr.send(form)
  }).then(() => {
    store.clearMessages()
    if (chatImportRef.value) chatImportRef.value.value = ''
  }).catch(e => {
    errorMessage.value = e.data?.detail ?? e.message ?? 'Falha no upload'
  }).finally(() => {
    store.loading = false
    uploadProgress.value = null
    disableUpload.value = true
  })
}

watch(
    () => store.chatActive.chatId,
    () => {
      disableUpload.value = true
      if (chatImportRef.value) {
        chatImportRef.value.value = ''
      }
    }
)
</script>

<template>
  <div class="chat-option mt-3 ">

    <button type="button" @click="toggleModal" class="btn btn-outline-primary btn-sm" data-bs-toggle="modal">
      Import/Export
    </button>

    <div class="modal" :class="modalClass" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title text-black">File importer/exporter</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" @click="toggleModal"
                    aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="form-control">
              <div class="alert alert-warning" v-if="errorMessage" role="alert">
                Failed to import<br/>
                {{ errorMessage }}
              </div>

              <div class="form-group d-flex justify-content-center mb-3">
                <a class="d-block text-center"
                   :href="linkDownload"
                   :download="chatName"
                >
                  Get the entire chat
                </a>
              </div>

              <div class="form-group mb-3 pt-4 border-top border-2" v-if="!allowDownloadAll">
                <p class="text-black">Attention, these actions are related to the selected chat! </p>
                <label for="formFileSm" class="form-label text-black">Import messages to this chat</label>
                <input class="form-control form-control-sm"
                       @change="onFilePicked"
                       accept=".zip,.txt"
                       id="formFileSm"
                       ref="chatImportRef"
                       type="file">
              </div>

              <div v-if="uploadProgress !== null && !allowDownloadAll" class="mb-3">
                <div v-if="uploadProgress < 100">
                  <div class="d-flex justify-content-between mb-1">
                    <small class="text-muted">{{ uploadStatusLabel }}</small>
                  </div>
                  <div class="progress" style="height: 8px;">
                    <div class="progress-bar progress-bar-striped progress-bar-animated"
                         role="progressbar"
                         :style="{ width: uploadProgress + '%' }"
                         :aria-valuenow="uploadProgress"
                         aria-valuemin="0"
                         aria-valuemax="100">
                    </div>
                  </div>
                </div>
                <div v-else class="d-flex align-items-center gap-2 text-muted">
                  <div class="spinner-border spinner-border-sm" role="status"></div>
                  <small>{{ uploadStatusLabel }}</small>
                </div>
              </div>

              <div class="btn-group" role="group" v-if="!allowDownloadAll">
                <button type="button" :disabled="disableUpload" @click="uploadFile"
                        class="btn btn-outline-secondary ml-2">Upload
                </button>
              </div>
            </div>


          </div>
          <div class="modal-footer">
            <button type="button" @click="toggleModal" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>

</style>
