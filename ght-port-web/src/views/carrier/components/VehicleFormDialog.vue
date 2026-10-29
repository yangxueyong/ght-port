<template>
  <el-dialog :model-value="modelValue" :title="form.id ? '编辑车辆' : '新增车辆'" width="720px" @update:model-value="emit('update:modelValue', $event)" @open="init">
    <div class="preview">
      <div class="road" />
      <TruckSvg :type="form.vehicleType" :length="form.vehicleLength || 9.6" :color="form.bodyColor" :width="previewWidth" />
      <div class="plate" v-if="form.plateNo">{{ form.plateNo }}</div>
    </div>
    <el-alert v-if="!form.id" type="info" :closable="false" show-icon class="mb">保存后将以“司机电话”为账号为司机开通登录账号（默认密码 123456），并赋予“司机”角色。</el-alert>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
      <el-form-item v-if="!selfMode" label="所属承运商" prop="carrierId">
        <el-select v-model="form.carrierId" filterable remote :remote-method="searchCarriers" placeholder="搜索承运商" style="width: 100%" @visible-change="(v) => v && searchCarriers('')">
          <el-option v-for="c in carriers" :key="c.id" :label="c.carrierName" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="车型" prop="vehicleType">
        <div class="types">
          <div v-for="t in VEHICLE_TYPES" :key="t.value" class="type" :class="{ on: form.vehicleType === t.value }" @click="pickType(t)">
            <TruckSvg :type="t.value" :length="t.length" :color="form.bodyColor" :width="76" />
            <span>{{ t.label }}</span>
          </div>
        </div>
      </el-form-item>
      <div class="grid2">
        <el-form-item label="车牌号" prop="plateNo"><el-input v-model="form.plateNo" placeholder="如：渝G·A1688" /></el-form-item>
        <el-form-item label="车身颜色">
          <div class="swatches">
            <span v-for="c in PALETTE_COLORS" :key="c" :style="{ background: c }" :class="{ on: form.bodyColor === c }" @click="form.bodyColor = c" />
          </div>
        </el-form-item>
        <el-form-item label="车长(米)"><el-input-number v-model="form.vehicleLength" :min="3" :max="18" :step="0.1" :precision="1" controls-position="right" style="width: 100%" /></el-form-item>
        <el-form-item label="载重(吨)"><el-input-number v-model="form.loadCapacity" :min="0" :step="1" controls-position="right" style="width: 100%" /></el-form-item>
        <el-form-item label="司机姓名" prop="driverName"><el-input v-model="form.driverName" /></el-form-item>
        <el-form-item label="司机电话" prop="driverPhone"><el-input v-model="form.driverPhone" maxlength="11" /></el-form-item>
      </div>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status"><el-radio value="ENABLED">启用</el-radio><el-radio value="DISABLED">停用</el-radio></el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import TruckSvg from '@/components/svg/TruckSvg.vue'
import { VEHICLE_TYPES, PALETTE_COLORS } from '@/utils/dict'
import { vehicleSave, carrierOptions } from '@/api/carrier'
import { withConfirm } from '@/utils/confirm'

const props = defineProps({ modelValue: Boolean, data: Object, carrierId: String, selfMode: Boolean })
const emit = defineEmits(['update:modelValue', 'saved'])
const formRef = ref()
const form = reactive({})
const saving = ref(false)
const carriers = ref([])
const previewWidth = computed(() => Math.round(110 + (form.vehicleLength || 9.6) * 14))
const rules = {
  carrierId: [{ required: true, message: '请选择所属承运商' }],
  vehicleType: [{ required: true, message: '请选择车型' }],
  plateNo: [{ required: true, message: '请输入车牌号' }],
  driverName: [{ required: true, message: '请输入司机姓名' }],
  driverPhone: [{ required: true, pattern: /^1\d{10}$/, message: '请输入正确的手机号' }]
}
async function init() {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, { vehicleType: 'HEAVY_CARGO', vehicleLength: 9.6, loadCapacity: 18, bodyColor: PALETTE_COLORS[1], status: 'ENABLED', carrierId: props.carrierId }, props.data || {})
  formRef.value?.clearValidate()
  if (!props.selfMode) {
    carriers.value = await carrierOptions('')
  }
}
function pickType(t) {
  form.vehicleType = t.value
  form.vehicleLength = t.length
}
async function searchCarriers(q) {
  carriers.value = await carrierOptions(q)
}
async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    const { id, carrierId, plateNo, vehicleType, vehicleLength, loadCapacity, bodyColor, driverName, driverPhone, status, remark } = form
    const res = await withConfirm(vehicleSave, { id, carrierId, plateNo, vehicleType, vehicleLength, loadCapacity, bodyColor, driverName, driverPhone, status, remark })
    if (!res) return
    ElMessage.success(form.id ? '已保存' : `已新增，司机账号：${form.driverPhone}`)
    emit('update:modelValue', false)
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.preview {
  position: relative; height: 120px; border-radius: 14px; margin: -6px 0 14px; overflow: hidden;
  display: flex; align-items: flex-end; justify-content: center; padding-bottom: 14px;
  background: linear-gradient(180deg, #e8f2ff 0%, #f6f9ff 70%, #dfe6f1 70%);
}
.road { position: absolute; left: 0; right: 0; bottom: 10px; height: 3px; background: repeating-linear-gradient(90deg, #b8c4d6 0 16px, transparent 16px 28px); }
.plate { position: absolute; right: 14px; top: 12px; padding: 3px 10px; border-radius: 6px; background: #ffd43b; border: 2px solid #1d2433; font-weight: 800; }
.mb { margin-bottom: 14px; }
.types { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; width: 100%; }
.type {
  display: flex; flex-direction: column; align-items: center; gap: 2px; padding: 8px 4px; border-radius: 12px; cursor: pointer;
  border: 1.5px solid var(--ght-line); font-size: 12px; transition: all 0.15s;
  &:hover { border-color: #bcd5f5; }
  &.on { border-color: var(--ght-blue); background: #f3f8ff; color: var(--ght-blue); font-weight: 600; }
}
.grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 0 12px; }
.swatches { display: flex; gap: 6px; flex-wrap: wrap; span { width: 22px; height: 22px; border-radius: 7px; cursor: pointer; border: 2px solid transparent; &.on { border-color: #fff; box-shadow: 0 0 0 2px var(--ght-blue); } } }
</style>
