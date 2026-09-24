<template>
  <span class="message-created-at" :class="classObject">{{ dateObject.date }}</span>
</template>

<script setup lang="ts">

const props = defineProps(['date'])

const dateObject = computed(() => {
  const now = new Date()
  const theDateMessage = new Date(props.date)
  const isTheSameYear = now.getFullYear() === theDateMessage.getFullYear()
  const isTheSameMonth = now.getMonth() === theDateMessage.getMonth()
  const isTheSameDay = now.getDate() === theDateMessage.getDate()

  if (isTheSameYear && isTheSameMonth && isTheSameDay) {

    return {
      date: theDateMessage.getHours().toString().padStart(2, '0') + ':' + theDateMessage.getMinutes().toString().padStart(2, '0'),
      today: true
    }
  } else {
    return reactive({
      date: theDateMessage.getDate().toString().padStart(2, '0') + '/' + (theDateMessage.getMonth() + 1).toString().padStart(2, '0'),
      today: false
    })
  }
})

const classObject = computed(() => {
  return {
    'font-weight-bold': dateObject.value.today
  }
})

</script>

<style scoped>
.message-created-at {
  font-size: 12px;
  color: var(--wa-text-2);
  white-space: nowrap;
}

.font-weight-bold {
  font-weight: 600;
}
</style>
