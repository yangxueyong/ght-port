<template>
  <el-dialog :model-value="modelValue" title="出门条已开具" width="440px" class="pass-dialog" @update:model-value="emit('update:modelValue', $event)">
    <PassTicket v-if="pass" :pass="pass" compact />
    <div v-if="pass" class="send">
      <div class="send-line"><el-icon color="#12b886"><CircleCheckFilled /></el-icon> 已通过短信将链接发送给司机 {{ pass.driverName }}（{{ pass.driverPhone }}）</div>
      <el-input :model-value="pass.link" readonly>
        <template #append><el-button icon="CopyDocument" @click="copy">复制链接</el-button></template>
      </el-input>
    </div>
    <template #footer>
      <el-button @click="open">打开司机端页面</el-button>
      <el-button type="primary" @click="emit('update:modelValue', false)">完成</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import PassTicket from '@/views/gatepass/components/PassTicket.vue'

const props = defineProps({ modelValue: Boolean, pass: Object })
const emit = defineEmits(['update:modelValue'])
async function copy() {
  try {
    await navigator.clipboard.writeText(props.pass.link)
    ElMessage.success('链接已复制')
  } catch (e) {
    ElMessage.info(props.pass.link)
  }
}
function open() {
  const url = `${location.origin}${location.pathname}#/pass/${props.pass.accessToken}`
  window.open(url, '_blank')
}
</script>

<style scoped>
.send { margin-top: 14px; display: flex; flex-direction: column; gap: 8px; }
.send-line { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--ght-ink-2); }
</style>
