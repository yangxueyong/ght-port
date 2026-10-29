<template>
  <div v-loading="loading" class="panel">
    <template v-if="d">
      <div class="hero" :style="{ '--brand': d.carrier.brandColor || '#1f7ae0' }">
        <div class="hero-scene">
          <div class="lane" />
          <div v-for="(v, i) in movingVehicles" :key="v.id" class="mover" :style="{ animationDelay: `${i * -3.2}s`, bottom: `${10 + (i % 2) * 22}px` }">
            <TruckSvg :type="v.vehicleType" :length="v.vehicleLength" :color="v.bodyColor || d.carrier.brandColor" moving :width="96" />
          </div>
        </div>
        <div class="hero-info">
          <div class="avatar">{{ (d.carrier.shortName || d.carrier.carrierName).slice(0, 2) }}</div>
          <div class="flex-1">
            <div class="name">{{ d.carrier.carrierName }}</div>
            <div class="sub">{{ d.carrier.contactName }} · {{ d.carrier.contactPhone }} · {{ d.carrier.address || '—' }}</div>
          </div>
          <StatusTag :dict="ENABLE_STATUS" :value="d.carrier.status" />
        </div>
      </div>

      <div class="stats">
        <div><span>车辆总数</span><b class="num">{{ d.carrier.vehicleCount }}</b></div>
        <div><span>运输中</span><b class="num t">{{ d.carrier.inTransitVehicleCount }}</b></div>
        <div><span>累计运输趟数</span><b class="num">{{ d.carrier.stat.tripCount }}</b></div>
        <div><span>累计运输吨数</span><b class="num">{{ fmtNum(d.carrier.stat.totalTonnage) }}</b></div>
        <div><span>累计运输方数</span><b class="num">{{ fmtNum(d.carrier.stat.totalVolume) }}</b></div>
        <div><span>累计运输件数</span><b class="num">{{ d.carrier.stat.totalPieces }}</b></div>
      </div>

      <el-tabs v-model="tab" class="tabs">
        <el-tab-pane :label="`车队 (${d.vehicles.length})`" name="fleet">
          <div class="flex-between mb">
            <el-radio-group v-model="vehicleFilter" size="small">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button value="IN_TRANSIT">运输中</el-radio-button>
              <el-radio-button value="IDLE">空闲</el-radio-button>
            </el-radio-group>
            <el-button v-if="has('vehicle:edit')" type="primary" size="small" icon="Plus" @click="editVehicle()">新增车辆</el-button>
          </div>
          <EmptyState v-if="!fleet.length" text="暂无车辆" />
          <div class="fleet">
            <div v-for="v in fleet" :key="v.id" class="v-card" :class="v.transportStatus">
              <div class="v-scene">
                <div v-if="v.transportStatus === 'IN_TRANSIT'" class="v-road" />
                <TruckSvg :type="v.vehicleType" :length="v.vehicleLength" :color="v.bodyColor || '#1f7ae0'" :moving="v.transportStatus === 'IN_TRANSIT'" :loaded="v.transportStatus === 'IN_TRANSIT'" :width="truckW(v)" />
              </div>
              <div class="v-info">
                <div class="flex-between">
                  <span class="plate">{{ v.plateNo }}</span>
                  <StatusTag :dict="VEHICLE_STATUS" :value="v.transportStatus" :pulse="v.transportStatus === 'IN_TRANSIT'" />
                </div>
                <div class="muted small">{{ v.vehicleTypeDesc }} · {{ v.vehicleLength || '-' }}米 · {{ v.loadCapacity || '-' }}吨 · {{ v.driverName }} {{ v.driverPhone }}</div>
                <div v-if="v.currentTrip" class="cur-trip">
                  <el-icon><Box /></el-icon>
                  <span class="ellipsis">{{ v.currentTrip.cargoName }}</span>
                  <b class="num">{{ fmtNum(v.currentTrip.loadTonnage, 3) }}吨</b>
                  <span class="muted">{{ fromNow(v.currentTrip.departTime) }}出发</span>
                </div>
                <div class="v-actions">
                  <el-button v-if="v.transportStatus === 'IN_TRANSIT' && has('trip:complete')" type="success" size="small" plain icon="CircleCheck" @click="complete({ vehicleId: v.id }, v.plateNo)">完成运输</el-button>
                  <span class="flex-1" />
                  <el-button v-if="has('vehicle:edit')" link type="primary" size="small" @click="editVehicle(v)">编辑</el-button>
                  <el-button v-if="has('vehicle:edit')" link type="danger" size="small" @click="removeVehicle(v)">删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="`运输中货物 (${d.activeTrips.length})`" name="active">
          <EmptyState v-if="!d.activeTrips.length" text="当前没有运输中的货物" />
          <div v-for="t in d.activeTrips" :key="t.id" class="trip-row">
            <TruckSvg :type="t.vehicleType" :length="9" :color="t.bodyColor || '#1f7ae0'" moving :width="90" />
            <div class="flex-1">
              <div><b>{{ t.plateNo }}</b> <span class="muted">· {{ t.driverName }}</span></div>
              <div class="small">{{ t.cargoName }} <span class="muted">（{{ t.cargoTypeName }}）</span> · 自 {{ t.berthName }}</div>
            </div>
            <div class="loads num">
              <span>{{ fmtNum(t.loadTonnage, 3) }}<small>吨</small></span>
              <span>{{ fmtNum(t.loadVolume, 3) }}<small>方</small></span>
              <span>{{ t.loadPieces }}<small>件</small></span>
            </div>
            <div class="muted small time">{{ fmtTime(t.departTime, 'MM-DD HH:mm') }}</div>
            <el-button v-if="has('trip:complete')" type="success" size="small" plain @click="complete({ tripId: t.id }, t.plateNo)">完成运输</el-button>
          </div>
        </el-tab-pane>

        <el-tab-pane label="运输记录" name="history" lazy>
          <el-table :data="trips.records" size="small">
            <el-table-column prop="tripNo" label="运输单号" width="170" />
            <el-table-column prop="plateNo" label="车牌" width="110" />
            <el-table-column prop="cargoName" label="货物" min-width="150" show-overflow-tooltip />
            <el-table-column label="装载量" width="170"><template #default="{ row }">{{ fmtNum(row.loadTonnage, 3) }}吨 / {{ fmtNum(row.loadVolume, 3) }}方 / {{ row.loadPieces }}件</template></el-table-column>
            <el-table-column label="出发" width="120"><template #default="{ row }">{{ fmtTime(row.departTime, 'MM-DD HH:mm') }}</template></el-table-column>
            <el-table-column label="状态" width="100"><template #default="{ row }"><StatusTag :dict="TRIP_STATUS" :value="row.status" /></template></el-table-column>
          </el-table>
          <el-pagination class="pager" layout="prev, pager, next, total" :total="trips.total || 0" :page-size="10" v-model:current-page="tripPage" @current-change="loadTrips" small />
        </el-tab-pane>
      </el-tabs>
    </template>

    <VehicleFormDialog v-model="vehicleDialog" :data="vehicleData" :carrier-id="d?.carrier.id" :self-mode="selfMode" @saved="refresh" />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import TruckSvg from '@/components/svg/TruckSvg.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import VehicleFormDialog from './VehicleFormDialog.vue'
import { ENABLE_STATUS, VEHICLE_STATUS, TRIP_STATUS } from '@/utils/dict'
import { fmtNum, fmtTime, fromNow } from '@/utils/format'
import { carrierDetail, carrierMine, tripComplete, tripPage as tripPageApi, vehicleDelete } from '@/api/carrier'
import { useUserStore } from '@/stores/user'

const props = defineProps({ carrierId: String, selfMode: Boolean })
const emit = defineEmits(['changed'])
const store = useUserStore()
const has = (p) => store.hasPerm(p)
const d = ref(null)
const loading = ref(false)
const tab = ref('fleet')
const vehicleFilter = ref('')
const vehicleDialog = ref(false)
const vehicleData = ref(null)
const trips = ref({ records: [], total: 0 })
const tripPage = ref(1)

const fleet = computed(() => (d.value?.vehicles || []).filter((v) => !vehicleFilter.value || v.transportStatus === vehicleFilter.value))
const movingVehicles = computed(() => (d.value?.vehicles || []).filter((v) => v.transportStatus === 'IN_TRANSIT').slice(0, 4))
const truckW = (v) => Math.round(80 + (Number(v.vehicleLength) || 9) * 6)

async function load() {
  loading.value = true
  try {
    d.value = props.selfMode ? await carrierMine() : await carrierDetail(props.carrierId)
    tripPage.value = 1
    loadTrips()
  } finally {
    loading.value = false
  }
}
async function loadTrips() {
  if (!d.value) return
  trips.value = await tripPageApi({ carrierId: d.value.carrier.id, pageNum: tripPage.value, pageSize: 10 })
}
async function refresh() {
  await load()
  emit('changed')
}
function editVehicle(v) {
  vehicleData.value = v ? { ...v } : null
  vehicleDialog.value = true
}
async function removeVehicle(v) {
  await ElMessageBox.confirm(`确定删除车辆【${v.plateNo}】吗？`, '删除确认', { type: 'warning' })
  await vehicleDelete(v.id)
  ElMessage.success('已删除')
  refresh()
}
async function complete(payload, plate) {
  await ElMessageBox.confirm(`确认车辆【${plate}】已完成本次运输？完成后车辆可再次接单出发。`, '完成运输', { type: 'info' })
  await tripComplete(payload)
  ElMessage.success('已设置为完成运输')
  refresh()
}

watch(() => [props.carrierId, props.selfMode], () => (props.selfMode || props.carrierId) && load(), { immediate: true })
defineExpose({ reload: load })
</script>

<style scoped lang="scss">
.hero { position: relative; border-radius: 16px; overflow: hidden; background: linear-gradient(135deg, color-mix(in srgb, var(--brand) 85%, #0b1847), var(--brand)); color: #fff; }
.hero-scene { position: relative; height: 92px; overflow: hidden; }
.lane { position: absolute; left: 0; right: 0; bottom: 8px; height: 46px; background: rgba(0, 0, 0, 0.15);
  &::after { content: ''; position: absolute; left: 0; right: 0; top: 22px; height: 2px; background: repeating-linear-gradient(90deg, rgba(255, 255, 255, 0.6) 0 18px, transparent 18px 32px); animation: lane 0.8s linear infinite; } }
@keyframes lane { to { background-position: -32px 0; } }
.mover { position: absolute; left: -110px; animation: drive 12.8s linear infinite; }
@keyframes drive { to { transform: translateX(calc(100vw)); } }
.hero-info { position: relative; display: flex; align-items: center; gap: 14px; padding: 4px 20px 18px; }
.avatar { width: 52px; height: 52px; border-radius: 16px; display: grid; place-items: center; font-weight: 800; font-size: 17px; background: rgba(255, 255, 255, 0.2); border: 1px solid rgba(255, 255, 255, 0.35); }
.name { font-size: 20px; font-weight: 700; }
.sub { font-size: 13px; opacity: 0.85; margin-top: 4px; }
.flex-1 { flex: 1; min-width: 0; }
.stats { display: grid; grid-template-columns: repeat(6, 1fr); gap: 10px; margin-top: 14px;
  div { background: #f6f8fc; border-radius: 12px; padding: 10px 12px; display: flex; flex-direction: column; gap: 2px; }
  span { font-size: 12px; color: var(--ght-ink-3); } b { font-size: 20px; &.t { color: var(--ght-transit); } } }
.tabs { margin-top: 8px; }
.mb { margin-bottom: 12px; }
.fleet { display: grid; grid-template-columns: repeat(auto-fill, minmax(290px, 1fr)); gap: 12px; }
.v-card { border: 1px solid var(--ght-line); border-radius: 14px; overflow: hidden; background: #fff; transition: box-shadow 0.2s;
  &:hover { box-shadow: var(--ght-shadow); }
  &.IN_TRANSIT { border-color: #cfe0f7; .v-scene { background: linear-gradient(180deg, #e6f1ff, #f7faff); } } }
.v-scene { position: relative; height: 84px; display: flex; align-items: flex-end; justify-content: center; padding-bottom: 8px; background: linear-gradient(180deg, #f4f6fa, #fbfcfe); }
.v-road { position: absolute; left: 0; right: 0; bottom: 8px; height: 2px; background: repeating-linear-gradient(90deg, #a9c3e6 0 12px, transparent 12px 22px); animation: lane 0.6s linear infinite; }
.v-info { padding: 10px 12px 12px; }
.plate { font-weight: 800; font-size: 16px; }
.small { font-size: 12px; }
.cur-trip { display: flex; align-items: center; gap: 6px; margin-top: 8px; padding: 6px 8px; border-radius: 8px; background: #f3f8ff; font-size: 12px; b { color: var(--ght-transit); } }
.ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 110px; }
.v-actions { display: flex; align-items: center; gap: 4px; margin-top: 10px; }
.trip-row { display: flex; align-items: center; gap: 14px; padding: 12px; border: 1px solid var(--ght-line); border-radius: 12px; margin-bottom: 10px;
  .loads { display: flex; gap: 12px; span { font-weight: 700; } small { color: var(--ght-ink-3); font-weight: 400; margin-left: 2px; } }
  .time { width: 80px; text-align: right; } }
.pager { margin-top: 12px; justify-content: flex-end; }
@media (max-width: 1200px) { .stats { grid-template-columns: repeat(3, 1fr); } }
</style>
