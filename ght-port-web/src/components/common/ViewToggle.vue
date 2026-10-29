<template>
  <div class="view-toggle">
    <button :class="{ on: modelValue === 'card' }" @click="set('card')">
      <el-icon><Grid /></el-icon><span>图文</span>
    </button>
    <button :class="{ on: modelValue === 'table' }" @click="set('table')">
      <el-icon><List /></el-icon><span>表格</span>
    </button>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { getItem, setItem } from '@/utils/storage'

const props = defineProps({ modelValue: { type: String, default: 'card' }, storageKey: String })
const emit = defineEmits(['update:modelValue'])

function set(v) {
  emit('update:modelValue', v)
  if (props.storageKey) setItem('ght_view_' + props.storageKey, v)
}
onMounted(() => {
  if (props.storageKey) {
    const saved = getItem('ght_view_' + props.storageKey)
    if (saved && saved !== props.modelValue) emit('update:modelValue', saved)
  }
})
</script>

<style scoped lang="scss">
.view-toggle {
  display: inline-flex;
  padding: 3px;
  border-radius: 10px;
  background: #e9eef7;
  button {
    display: flex; align-items: center; gap: 4px;
    border: 0; background: transparent; padding: 6px 12px; border-radius: 8px;
    color: var(--ght-ink-3); cursor: pointer; font-size: 13px;
    &.on { background: #fff; color: var(--ght-blue); box-shadow: var(--ght-shadow-sm); font-weight: 600; }
  }
}
</style>
