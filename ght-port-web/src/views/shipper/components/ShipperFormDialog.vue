<template>
  <el-dialog :model-value="modelValue" :title="form.id ? '编辑船运公司' : '新增船运公司'" width="520px" @update:model-value="emit('update:modelValue', $event)" @open="init">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
      <el-form-item label="船运公司名称" prop="companyName"><el-input v-model="form.companyName" /></el-form-item>
      <el-form-item label="简称"><el-input v-model="form.shortName" /></el-form-item>
      <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
      <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
      <el-form-item label="信用代码"><el-input v-model="form.creditCode" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { shipperSave } from '@/api/shipper'

const props = defineProps({ modelValue: Boolean, data: Object })
const emit = defineEmits(['update:modelValue', 'saved'])
const formRef = ref()
const form = reactive({})
const rules = { companyName: [{ required: true, message: '请输入船运公司名称' }] }
function init() {
  Object.keys(form).forEach((k) => delete form[k])
  const { id, companyName, shortName, contactName, contactPhone, creditCode, address, remark } = props.data || {}
  Object.assign(form, { id, companyName, shortName, contactName, contactPhone, creditCode, address, remark })
  formRef.value?.clearValidate()
}
async function submit() {
  await formRef.value.validate()
  await shipperSave(form)
  ElMessage.success('已保存')
  emit('update:modelValue', false)
  emit('saved')
}
</script>
