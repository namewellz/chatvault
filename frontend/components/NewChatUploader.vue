<script setup lang="ts">
import {useMainStore} from "~/store";

const store = useMainStore()
const emit = defineEmits(['update:chats', 'exit:dialog'])
const chatImportRef = ref(null)
const importChatResult = ref({data: null, errorMessage: null})
const fileValid = ref(false)
const chatName = ref(null)
const uploadProgress = ref<number | null>(null)

const importChatPath = computed(() => {
  if (chatName.value != null) {
    return useRuntimeConfig().public.api.importChatByName.replace(":chatName", chatName.value);
  }
})

const chatNameValid = computed(() => chatName.value !== null && chatName.value.trim() !== '')

const disableUpload = computed(() => {
  return !chatNameValid.value || fileValid.value === false || uploadProgress.value !== null
})

const uploadStatusLabel = computed(() => {
  if (uploadProgress.value === null) return null
  if (uploadProgress.value < 100) return `Enviando... ${uploadProgress.value}%`
  return 'Processando arquivo...'
})

async function onFilePicked() {
  if (chatImportRef?.value?.files && chatImportRef?.value?.files[0]) {
    fileValid.value = true
  }
}

async function uploadFile() {
  if (!chatImportRef?.value?.files || !chatImportRef?.value?.files[0]) return

  store.loading = true
  importChatResult.value.errorMessage = null
  uploadProgress.value = 0

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
    emit('update:chats')
    if (chatImportRef.value) chatImportRef.value.value = ''
    fileValid.value = false
  }).catch(e => {
    importChatResult.value.errorMessage = e.data?.detail ?? e.message ?? 'Falha no upload'
  }).finally(() => {
    store.loading = false
    uploadProgress.value = null
  })
}

function cancel() {
  emit('exit:dialog')
}

</script>

<template>
  <div class="m-auto col-md-3">
    <div class="form-control">
      <div class="alert alert-warning" v-if="importChatResult.errorMessage" role="alert">
        Failed to import.<br/>
        {{ importChatResult.errorMessage }}
      </div>

      <div class="form-group">
        <label for="formGroupExampleInput">Give this chat a name</label>
        <input type="text" class="form-control" id="formGroupExampleInput" v-model="chatName" placeholder="Chat name">
      </div>

      <div class="form-group mb-3 pt-4 border-top border-2">
        <label for="formFileSm" class="form-label text-black">Import a new chat</label>
        <input class="form-control form-control-sm"
               @change="onFilePicked"
               accept=".zip,.txt"
               id="formFileSm"
               ref="chatImportRef"
               type="file">
      </div>

      <div v-if="uploadProgress !== null" class="mb-3">
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

      <div class="btn-group" role="group">
        <button type="button" :disabled="disableUpload" @click="uploadFile"
                class="btn btn-outline-secondary ml-2">Upload
        </button>
        <button type="button" @click="cancel"
                class="btn btn-outline-secondary ml-2">Cancel
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>
