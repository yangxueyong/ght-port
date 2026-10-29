<template>
  <el-dialog :model-value="modelValue" :title="isLoad ? '泊位装货' : '泊位到货'" width="560px" @update:model-value="emit('update:modelValue', $event)" @open="reset">
    <div class="berth-line">
      <el-icon><Ship /></el-icon>
      <span>{{ berth?.berthName }}（{{ berth?.berthCode }}）</span>
      <StatusTag :dict="BERTH_STATUS" :value="berth?.status" />
    </div>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
      <el-form-item label="货物类型" prop="cargoTypeId">
        <el-select v-model="form.cargoTypeId" filterable remote :remote-method="searchTypes" :loading="typeLoading" placeholder="输入搜索货物类型" style="width: 100%" @visible-change="(v) => v && !types.length && searchTypes('')">
          <el-option v-for="t in types" :key="t.id" :label="t.typeName" :value="t.id">
            <span class="type-dot" :style="{ background: t.color || '#1f7ae0' }" />{{ t.typeName }}
            <span class="muted" style="float: right; font-size: 12px">{{ t.typeCode }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="货物名称" prop="cargoName"><el-input v-model="form.cargoName" :placeholder="isLoad ? '如：玉米（散粮）' : '如：动力煤（5500大卡）'" /></el-form-item>
      <el-form-item label="货物形态" prop="packType">
        <el-radio-group v-model="form.packType" class="pack-radios">
          <el-radio-button v-for="t in PACK_TYPES" :key="t.value" :value="t.value">{{ t.label }}</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="货物吨位" prop="tonnage"><el-input-number v-model="form.tonnage" :min="0" :precision="3" controls-position="right" class="num-input" /><span class="unit">吨</span></el-form-item>
      <el-form-item label="方数 / 件数" class="optional">
        <div class="opt-row">
          <el-input-number v-model="form.volume" :min="0" :precision="3" :controls="false" placeholder="选填" /><span class="unit">方</span>
          <el-input-number v-model="form.pieces" :min="0" :controls="false" placeholder="选填" /><span class="unit">{{ form.packType === 'CONTAINER' ? '箱' : '件' }}</span>
        </div>
      </el-form-item>
      <el-form-item v-if="!isLoad" label="所属船运公司" prop="shipIds">
        <el-select v-model="form.shipIds" popper-class="ght-rich-option" multiple filterable remote :remote-method="searchShips" :loading="shipLoading" placeholder="输入船名或船运公司搜索船只（可多选）" style="width: 100%"
          @visible-change="(v) => v && !ships.length && searchShips('')">
          <el-option v-for="s in ships" :key="s.id" :label="`${s.companyName} / ${s.shipName}`" :value="s.id">
            <div class="ship-opt">
              <ShipSvg :type="s.shipType" :length="s.shipLength" :color="s.hullColor || '#1f6fd1'" :width="56" />
              <div>
                <div>{{ s.shipName }} <span class="muted">· {{ s.shipTypeDesc }}</span></div>
                <div class="muted small">{{ s.companyName }}</div>
              </div>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">{{ isLoad ? '确认装货' : '确认到货' }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/common/StatusTag.vue'
import ShipSvg from '@/components/svg/ShipSvg.vue'
import { BERTH_STATUS, PACK_TYPES } from '@/utils/dict'
import { berthArrive, berthLoad, cargoTypeList } from '@/api/berth'
import { shipOptions } from '@/api/shipper'

/** mode: arrive 泊位到货（关联船运公司船只）| load 泊位装货（登记后再分派船只） */
const props = defineProps({ modelValue: Boolean, berth: Object, mode: { type: String, default: 'arrive' } })
const emit = defineEmits(['update:modelValue', 'done'])
const formRef = ref()
const form = reactive({})
const saving = ref(false)
const isLoad = computed(() => props.mode === 'load')
const rules = {
  packType: [{ required: true, message: '请选择货物形态' }],
  cargoTypeId: [{ required: true, message: '请选择货物类型' }],
  cargoName: [{ required: true, message: '请输入货物名称' }],
  tonnage: [{ required: true, type: 'number', min: 0.001, message: '请输入货物吨位', trigger: 'change' }],
  shipIds: [{ required: true, type: 'array', min: 1, message: '请选择所属船运公司船只' }]
}
const types = ref([])
const typeLoading = ref(false)
const ships = ref([])
const shipLoading = ref(false)

function reset() {
  Object.assign(form, { cargoTypeId: null, cargoName: '', packType: 'BULK', tonnage: undefined, volume: undefined, pieces: undefined, shipIds: [], remark: '' })
  formRef.value?.clearValidate()
}
async function searchTypes(q) {
  typeLoading.value = true
  try {
    types.value = await cargoTypeList(q)
  } finally {
    typeLoading.value = false
  }
}
async function searchShips(q) {
  shipLoading.value = true
  try {
    const list = await shipOptions(q)
    const keep = ships.value.filter((s) => form.shipIds.includes(s.id) && !list.find((x) => x.id === s.id))
    ships.value = [...keep, ...list]
  } finally {
    shipLoading.value = false
  }
}
async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    const action = isLoad.value ? '装货' : '到货'
    if (isLoad.value) {
      const { shipIds, ...rest } = form
      await berthLoad({ ...rest, berthId: props.berth.id })
    } else {
      await berthArrive({ ...form, berthId: props.berth.id })
    }
    ElMessage.success(props.berth.status === 'IDLE' ? `${action}登记成功，泊位已进入作业状态` : `${action}登记成功`)
    emit('update:modelValue', false)
    emit('done')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.berth-line { display: flex; align-items: center; gap: 8px; margin: -6px 0 16px; padding: 10px 12px; border-radius: 10px; background: #f6f8fc; font-weight: 600; }
.num-input { width: 200px; }
.unit { margin: 0 14px 0 8px; color: var(--ght-ink-3); }
.opt-row { display: flex; align-items: center; :deep(.el-input-number) { width: 130px; } }
.optional :deep(.el-form-item__label) { color: var(--ght-ink-3); }
.type-dot { display: inline-block; width: 8px; height: 8px; border-radius: 3px; margin-right: 8px; }
.ship-opt { display: flex; align-items: center; gap: 10px; line-height: 1.3; padding: 2px 0; }
.small { font-size: 12px; }
</style>
