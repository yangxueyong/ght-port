<template>
  <el-dialog :model-value="modelValue" :title="form.id ? '编辑承运商' : '新增承运商'" width="560px" @update:model-value="emit('update:modelValue', $event)" @open="init">
    <el-alert v-if="!form.id" type="info" :closable="false" show-icon class="mb">
      保存后将以“联系电话”为账号自动开通承运商登录账号（默认密码 123456），并赋予“承运商”角色。
    </el-alert>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
      <el-form-item label="承运商名称" prop="carrierName"><el-input v-model="form.carrierName" /></el-form-item>
      <el-form-item label="简称"><el-input v-model="form.shortName" /></el-form-item>
      <div class="grid2">
        <el-form-item label="联系人" prop="contactName"><el-input v-model="form.contactName" /></el-form-item>
        <el-form-item label="联系电话" prop="contactPhone"><el-input v-model="form.contactPhone" maxlength="11" /></el-form-item>
      </div>
      <el-form-item label="信用代码"><el-input v-model="form.creditCode" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="品牌色">
        <div class="swatches">
          <span v-for="c in PALETTE_COLORS" :key="c" :style="{ background: c }" :class="{ on: form.brandColor === c }" @click="form.brandColor = c" />
        </div>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status"><el-radio value="ENABLED">启用</el-radio><el-radio value="DISABLED">停用</el-radio></el-radio-group>
      </el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { carrierSave } from '@/api/carrier'
import { PALETTE_COLORS } from '@/utils/dict'

const props = defineProps({ modelValue: Boolean, data: Object })
const emit = defineEmits(['update:modelValue', 'saved'])
const formRef = ref()
const form = reactive({})
const saving = ref(false)
const rules = {
  carrierName: [{ required: true, message: '请输入承运商名称' }],
  contactName: [{ required: true, message: '请输入联系人' }],
  contactPhone: [{ required: true, pattern: /^1\d{10}$/, message: '请输入正确的手机号' }]
}
function init() {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, { status: 'ENABLED', brandColor: PALETTE_COLORS[0] }, props.data || {})
  formRef.value?.clearValidate()
}
async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    const { id, carrierName, shortName, contactName, contactPhone, address, creditCode, brandColor, status, remark } = form
    await carrierSave({ id, carrierName, shortName, contactName, contactPhone, address, creditCode, brandColor, status, remark })
    ElMessage.success(form.id ? '已保存' : `已新增，承运商账号：${form.contactPhone}`)
    emit('update:modelValue', false)
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.mb { margin-bottom: 16px; }
.grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 0 12px; }
.swatches { display: flex; gap: 8px; flex-wrap: wrap; span { width: 26px; height: 26px; border-radius: 8px; cursor: pointer; border: 2px solid transparent; transition: transform 0.15s; &:hover { transform: scale(1.1); } &.on { border-color: #fff; box-shadow: 0 0 0 2px var(--ght-blue); } } }
</style>
