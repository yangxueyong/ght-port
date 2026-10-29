<template>
  <el-drawer :model-value="modelValue" size="760px" :title="`分派承运 · ${berth?.berthName || ''}`" @update:model-value="emit('update:modelValue', $event)" @open="init">
    <div class="assign-box">
      <div class="section-title">选择承运商与货车</div>
      <el-form :model="form" label-width="78px" class="mt-12">
        <el-form-item label="承运货物">
          <el-radio-group v-model="form.cargoId" class="cargo-radios">
            <el-radio-button v-for="c in unloadCargos" :key="c.id" :value="c.id">{{ c.cargoName }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="承运商">
          <el-select v-model="form.carrierId" filterable remote :remote-method="searchCarriers" :loading="carrierLoading" placeholder="输入搜索承运商" style="width: 100%"
            @visible-change="(v) => v && !carriers.length && searchCarriers('')" @change="onCarrierChange">
            <el-option v-for="c in carriers" :key="c.id" :label="c.carrierName" :value="c.id">
              <span class="brand-dot" :style="{ background: c.brandColor || '#1f7ae0' }" />{{ c.carrierName }}
              <span class="muted" style="float: right; font-size: 12px">{{ c.contactName }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="货车">
          <el-select v-model="form.vehicleIds" popper-class="ght-rich-option" multiple filterable remote :remote-method="searchVehicles" :loading="vehicleLoading" :disabled="!form.carrierId"
            placeholder="只能选择该承运商下的车辆（可多选）" style="width: 100%" @visible-change="(v) => v && searchVehicles('')">
            <el-option v-for="v in vehicles" :key="v.id" :label="v.plateNo" :value="v.id">
              <div class="veh-opt">
                <TruckSvg :type="v.vehicleType" :length="v.vehicleLength" :color="v.bodyColor || '#1f7ae0'" :width="70" />
                <div>
                  <div><b>{{ v.plateNo }}</b> <span class="muted">· {{ v.vehicleTypeDesc }}</span></div>
                  <div class="muted small">{{ v.driverName }} · {{ v.transportStatus === 'IN_TRANSIT' ? '运输中' : '空闲' }}</div>
                </div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <div class="assign-actions">
          <span class="muted small">提示：“确定”可重复点击，同一辆车可对同一货物多次运输</span>
          <el-button type="primary" icon="Check" :loading="assigning" @click="assign">确定</el-button>
        </div>
      </el-form>
    </div>

    <div class="flex-between mt-16">
      <div class="section-title">分派的货车（{{ list.length }}）</div>
      <el-radio-group v-model="filter" size="small">
        <el-radio-button value="ALL">全部</el-radio-button>
        <el-radio-button value="WAITING">待装载</el-radio-button>
        <el-radio-button value="DEPARTED">已出发</el-radio-button>
      </el-radio-group>
    </div>
    <div class="mt-12">
      <DispatchList :dispatches="filtered" :can-depart="canDepart" :can-assign="canAssign" empty-text="请在上方选择承运商与货车后点击“确定”" @changed="reload" />
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import TruckSvg from '@/components/svg/TruckSvg.vue'
import DispatchList from './DispatchList.vue'
import { carrierOptions, vehicleOptions } from '@/api/carrier'
import { dispatchAssign, dispatchList } from '@/api/berth'
import { useUserStore } from '@/stores/user'

const props = defineProps({ modelValue: Boolean, berth: Object, cargos: { type: Array, default: () => [] }, cargoId: String })
const emit = defineEmits(['update:modelValue', 'changed'])
const store = useUserStore()
const canDepart = computed(() => store.hasPerm('berth:dispatch:depart'))
const canAssign = computed(() => store.hasPerm('berth:dispatch:assign'))

const form = reactive({ cargoId: null, carrierId: null, vehicleIds: [] })
const carriers = ref([])
const vehicles = ref([])
const carrierLoading = ref(false)
const vehicleLoading = ref(false)
const assigning = ref(false)
const list = ref([])
const filter = ref('ALL')
const unloadCargos = computed(() => props.cargos.filter((c) => c.direction !== 'LOAD'))
const filtered = computed(() => list.value.filter((d) => d.cargoId === form.cargoId && (filter.value === 'ALL' || d.status === filter.value)))

function init() {
  form.cargoId = (unloadCargos.value.find((c) => c.id === props.cargoId) || unloadCargos.value[0])?.id || null
  form.vehicleIds = []
  reload()
}
async function reload() {
  if (!props.berth) return
  list.value = await dispatchList({ berthId: props.berth.id, statuses: ['WAITING', 'DEPARTED'] })
  emit('changed')
}
async function searchCarriers(q) {
  carrierLoading.value = true
  try {
    carriers.value = await carrierOptions(q)
  } finally {
    carrierLoading.value = false
  }
}
function onCarrierChange() {
  form.vehicleIds = []
  vehicles.value = []
}
async function searchVehicles(q) {
  if (!form.carrierId) return
  vehicleLoading.value = true
  try {
    const res = await vehicleOptions(form.carrierId, q)
    const keep = vehicles.value.filter((v) => form.vehicleIds.includes(v.id) && !res.find((x) => x.id === v.id))
    vehicles.value = [...keep, ...res]
  } finally {
    vehicleLoading.value = false
  }
}
async function assign() {
  if (!form.cargoId) return ElMessage.warning('请选择承运货物')
  if (!form.carrierId) return ElMessage.warning('请选择承运商')
  if (!form.vehicleIds.length) return ElMessage.warning('请选择货车')
  assigning.value = true
  try {
    const added = await dispatchAssign({ berthId: props.berth.id, ...form })
    ElMessage.success(`已分派 ${added.length} 辆货车，请填写装载量后出发`)
    filter.value = 'ALL'
    await reload()
  } finally {
    assigning.value = false
  }
}
</script>

<style scoped lang="scss">
.assign-box { padding: 14px 16px; border-radius: 14px; background: linear-gradient(180deg, #f5f9ff, #fff); border: 1px solid #dfeafa; }
.cargo-radios { flex-wrap: wrap; }
.brand-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 8px; }
.veh-opt { display: flex; align-items: center; gap: 10px; line-height: 1.35; padding: 3px 0; }
.small { font-size: 12px; }
.assign-actions { display: flex; align-items: center; justify-content: space-between; }
</style>
