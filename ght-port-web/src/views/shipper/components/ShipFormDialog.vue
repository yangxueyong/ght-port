<template>
  <el-dialog :model-value="modelValue" :title="form.id ? '编辑船只' : '新增船只'" width="640px" @update:model-value="emit('update:modelValue', $event)" @open="init">
    <div class="preview">
      <ShipSvg :type="form.shipType" :length="form.shipLength || 90" :color="form.hullColor" :name="form.shipName" sailing :width="360" />
    </div>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
      <el-form-item label="所属船运公司"><el-input :model-value="companyName" disabled /></el-form-item>
      <el-form-item label="船舶类型" prop="shipType">
        <div class="types">
          <div v-for="t in SHIP_TYPES" :key="t.value" class="type" :class="{ on: form.shipType === t.value }" @click="form.shipType = t.value">
            <ShipSvg :type="t.value" :length="80" :color="form.hullColor" :width="84" />
            <span>{{ t.label }}</span>
          </div>
        </div>
      </el-form-item>
      <div class="grid2">
        <el-form-item label="船名" prop="shipName"><el-input v-model="form.shipName" /></el-form-item>
        <el-form-item label="识别号"><el-input v-model="form.shipNo" /></el-form-item>
        <el-form-item label="船长(米)"><el-input-number v-model="form.shipLength" :min="20" :max="200" controls-position="right" style="width: 100%" /></el-form-item>
        <el-form-item label="载重吨"><el-input-number v-model="form.deadweight" :min="0" :step="100" controls-position="right" style="width: 100%" /></el-form-item>
        <el-form-item label="船长姓名"><el-input v-model="form.captainName" /></el-form-item>
        <el-form-item label="船长电话"><el-input v-model="form.captainPhone" /></el-form-item>
      </div>
      <el-form-item label="船体颜色">
        <div class="swatches">
          <span v-for="c in PALETTE_COLORS" :key="c" :style="{ background: c }" :class="{ on: form.hullColor === c }" @click="form.hullColor = c" />
        </div>
      </el-form-item>
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
import ShipSvg from '@/components/svg/ShipSvg.vue'
import { SHIP_TYPES, PALETTE_COLORS } from '@/utils/dict'
import { shipSave } from '@/api/shipper'

const props = defineProps({ modelValue: Boolean, data: Object, companyId: String, companyName: String })
const emit = defineEmits(['update:modelValue', 'saved'])
const formRef = ref()
const form = reactive({})
const rules = { shipType: [{ required: true, message: '请选择船舶类型' }], shipName: [{ required: true, message: '请输入船名' }] }
function init() {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, { shipType: 'BULK', shipLength: 90, hullColor: PALETTE_COLORS[1] }, props.data || {}, { companyId: props.companyId })
  delete form.companyName
  delete form.shipTypeDesc
  formRef.value?.clearValidate()
}
async function submit() {
  await formRef.value.validate()
  const { id, companyId, shipName, shipNo, shipType, shipLength, deadweight, hullColor, captainName, captainPhone, remark } = form
  await shipSave({ id, companyId, shipName, shipNo, shipType, shipLength, deadweight, hullColor, captainName, captainPhone, remark })
  ElMessage.success('已保存')
  emit('update:modelValue', false)
  emit('saved')
}
</script>

<style scoped lang="scss">
.preview { display: flex; justify-content: center; padding: 10px 0 14px; margin: -6px 0 12px; border-radius: 14px; background: linear-gradient(180deg, #eaf4ff, #f7fbff); }
.types { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px; width: 100%; }
.type { display: flex; flex-direction: column; align-items: center; min-width: 0; overflow: hidden; padding: 6px 2px; line-height: 1.6; border-radius: 10px; border: 1.5px solid var(--ght-line); cursor: pointer; font-size: 12px;
  &.on { border-color: var(--ght-blue); background: #f3f8ff; color: var(--ght-blue); font-weight: 600; } }
.grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 0 12px; }
.swatches { display: flex; gap: 6px; span { width: 22px; height: 22px; border-radius: 7px; cursor: pointer; border: 2px solid transparent; &.on { border-color: #fff; box-shadow: 0 0 0 2px var(--ght-blue); } } }
</style>
