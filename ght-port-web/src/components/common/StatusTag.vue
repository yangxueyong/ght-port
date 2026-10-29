<template>
  <span class="ght-tag" :style="{ color: meta.color, background: meta.bg }">
    <i class="status-dot" :class="{ pulse }" :style="{ background: meta.color, color: meta.color }" />
    {{ meta.label }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ dict: { type: Object, required: true }, value: String, pulse: Boolean })
const meta = computed(() => props.dict[props.value] || { label: props.value || '-', color: '#6b7a92', bg: '#f1f3f7' })
</script>
