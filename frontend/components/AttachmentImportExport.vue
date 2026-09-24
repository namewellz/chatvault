<script setup lang="ts">
import { useMainStore } from "~/store";

const store = useMainStore();

const fileInputRef = ref<HTMLInputElement | null>(null);
const overwrite = ref(false);
const disableUpload = ref(true);
const errorMessage = ref<string | null>(null);
const successMessage = ref<string | null>(null);
const uploadProgress = ref<number | null>(null);

const exportCsvUrl = computed(() =>
    useRuntimeConfig().public.api.exportAttachmentsCsv.replace(":chatId", store.chatActive?.chatId?.toString())
);
const importZipUrl = computed(() =>
    useRuntimeConfig().public.api.importAttachmentsZip.replace(":chatId", store.chatActive?.chatId?.toString())
);

function onFilePicked() {
  disableUpload.value = !(fileInputRef.value?.files && fileInputRef.value.files[0]);
  errorMessage.value = null;
  successMessage.value = null;
}

async function uploadFile() {
  const file = fileInputRef.value?.files?.[0];
  if (!file) return;

  store.loading = true;
  errorMessage.value = null;
  successMessage.value = null;
  uploadProgress.value = 0;
  disableUpload.value = true;

  const form = new FormData();
  form.append("file", file);
  form.append("overwrite", overwrite.value ? "true" : "false");

  await new Promise<{ imported?: number; skipped?: number; ignored?: number }>((resolve, reject) => {
    const xhr = new XMLHttpRequest();

    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) {
        uploadProgress.value = Math.min(99, Math.round((event.loaded / event.total) * 100));
      }
    };

    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        uploadProgress.value = 100;
        try {
          resolve(JSON.parse(xhr.responseText) ?? {});
        } catch {
          resolve({});
        }
      } else {
        try {
          reject({ data: JSON.parse(xhr.responseText) });
        } catch {
          reject({ message: `Erro HTTP ${xhr.status}` });
        }
      }
    };

    xhr.onerror = () => reject({ message: "Falha de conexão" });
    xhr.onabort = () => reject({ message: "Upload cancelado" });

    xhr.open("POST", importZipUrl.value);
    xhr.send(form);
  }).then((counts) => {
    if (fileInputRef.value) fileInputRef.value.value = "";
    successMessage.value =
        `Anexos processados: ${counts.imported ?? 0} importados, ` +
        `${counts.skipped ?? 0} pulados (já existem), ${counts.ignored ?? 0} ignorados (sem mensagem correspondente).`;
  }).catch((e) => {
    errorMessage.value = e.data?.detail ?? e.message ?? "Falha no upload";
  }).finally(() => {
    store.loading = false;
    uploadProgress.value = null;
    disableUpload.value = true;
  });
}
</script>

<template>
  <div class="attachment-io mt-4 pt-3 border-top border-secondary">
    <label class="form-label">Anexos da conversa</label>

    <a class="btn btn-outline-primary btn-sm w-100" :href="exportCsvUrl" download>
      Exportar lista de anexos (CSV)
    </a>

    <div class="mt-3">
      <label for="attachment-zip" class="form-label">Importar anexos (ZIP)</label>
      <input
          class="form-control form-control-sm"
          id="attachment-zip"
          type="file"
          accept=".zip"
          ref="fileInputRef"
          @change="onFilePicked"
      />
      <div class="form-check mt-2">
        <input class="form-check-input" type="checkbox" id="overwrite-media" v-model="overwrite"/>
        <label class="form-check-label" for="overwrite-media">Sobrescrever mídias existentes</label>
      </div>
      <button
          type="button"
          class="btn btn-outline-secondary btn-sm mt-2"
          :disabled="disableUpload"
          @click="uploadFile"
      >
        Enviar
      </button>
    </div>

    <div v-if="uploadProgress !== null" class="mt-2">
      <div class="progress" style="height: 8px;">
        <div
            class="progress-bar progress-bar-striped progress-bar-animated"
            role="progressbar"
            :style="{ width: uploadProgress + '%' }"
            :aria-valuenow="uploadProgress"
            aria-valuemin="0"
            aria-valuemax="100"
        ></div>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-warning mt-2" role="alert">
      Failed to import<br/>
      {{ errorMessage }}
    </div>
    <div v-if="successMessage" class="alert alert-success mt-2" role="alert">
      {{ successMessage }}
    </div>
  </div>
</template>

<style scoped>
.form-label {
  font-size: 13px;
  color: var(--wa-text-2);
}

.form-check-label {
  font-size: 13px;
  color: var(--wa-text-1);
}
</style>
