<template>
  <span :class="isToday ? 'font-semibold text-wa-green' : 'text-wa-text-muted'" class="text-[12px] whitespace-nowrap">
    {{ formattedDate }}
  </span>
</template>

<script setup lang="ts">
import { useDateFormatting } from '~/composables/useDateFormatting'
import { isToday as isTodayUtil, formatDate as utilFormatDate } from '~/utils/dateFormatter'

const props = defineProps(['date'])
const { locale } = useDateFormatting()

const formattedDate = computed(() => {
  const result = utilFormatDate(props.date, locale.value)
  return result.formattedDate
})

const isToday = computed(() => {
  if (!props.date) return false
  return isTodayUtil(new Date(props.date))
})
</script>
