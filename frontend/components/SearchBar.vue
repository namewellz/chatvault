<!-- SearchBar is now integrated into MessageAreaNavBar. -->
<!-- This component is kept for backwards compatibility but renders nothing. -->
<template><span /></template>
<script setup lang="ts">
defineProps({ chatId: { type: [Number, null], default: null } })
defineEmits(['search'])
</script>
