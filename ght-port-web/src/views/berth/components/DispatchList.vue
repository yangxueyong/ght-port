<template>
  <div class="dispatch-list">
    <EmptyState v-if="!dispatches.length" :text="emptyText" />
    <transition-group name="list" tag="div" class="cards">
      <div v-for="d in dispatches" :key="d.id" class="d-card" :class="d.status">
        <div class="d-top">
          <div class="truck-wrap">
            <TruckSvg :type="d.vehicleType" :length="d.vehicleLength" :color="d.bodyColor || '#1f7ae0'" :moving="isMoving(d)" :loaded="d.status === 'DEPARTED'" :width="104" />
            <div v-if="isMoving(d)" class="road" />
          </div>
          <div class="d-meta">
            <div class="plate-line">
              <span class="plate">{{ d.plateNo }}</span>
              <StatusTag :dict="DISPATCH_STATUS" :value="d.status" />
            </div>
            <div class="muted small one-line" :title="d.carrierName">{{ d.carrierName }}</div>
            <div class="muted small one-line">{{ d.driverName }} · {{ d.driverPhone }}</div>
          </div>
        </div>
        <div class="cargo-line"><el-icon><Box /></el-icon>{{ d.cargoName }}</div>

        <template v-if="d.status === 'WAITING'">
          <div class="loads">
            <label><span><i class="star">*</i>装载吨数</span><el-input-number v-model="loads[d.id].loadTonnage" :min="0" :precision="3" :controls="false" size="small" placeholder="请输入" /></label>
            <label>装载方数<el-input-number v-model="loads[d.id].loadVolume" :min="0" :precision="3" :controls="false" size="small" placeholder="选填" /></label>
            <label>装载件数<el-input-number v-model="loads[d.id].loadPieces" :min="0" :controls="false" size="small" placeholder="选填" /></label>
          </div>
          <div class="d-actions">
            <el-button v-if="canAssign" link type="info" size="small" @click="cancel(d)">取消分派</el-button>
            <el-tooltip :disabled="d.vehicleTransportStatus !== 'IN_TRANSIT'" content="该车辆尚未完成上一次运输，完成运输后才能再次出发">
              <span>
                <el-button v-if="canDepart" type="primary" size="small" icon="Promotion" :loading="busy[d.id]" :disabled="d.vehicleTransportStatus === 'IN_TRANSIT'" @click="depart(d)">
                  装载完成，立即出发
                </el-button>
              </span>
            </el-tooltip>
          </div>
        </template>

        <template v-else-if="d.trip">
          <div class="trip">
            <div><b class="num">{{ fmtNum(d.trip.loadTonnage, 3) }}</b><small>吨</small></div>
            <div><b class="num">{{ fmtNum(d.trip.loadVolume, 3) }}</b><small>方</small></div>
            <div><b class="num">{{ d.trip.loadPieces || 0 }}</b><small>件</small></div>
          </div>
          <div class="d-actions">
            <span class="muted small">出发于 {{ fmtTime(d.trip.departTime, 'MM-DD HH:mm') }}</span>
            <span class="flex-center gap-8">
              <StatusTag :dict="TRIP_STATUS" :value="d.trip.status" :pulse="d.trip.status === 'IN_TRANSIT'" />
              <StatusTag v-if="d.passStatus" :dict="PASS_STATUS" :value="d.passStatus" />
              <el-button v-if="d.passToken" link type="primary" size="small" @click="showPass(d)">出门条</el-button>
            </span>
          </div>
        </template>
      </div>
    </transition-group>
    <GatePassDialog v-model="passVisible" :pass="pass" />
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import TruckSvg from '@/components/svg/TruckSvg.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import GatePassDialog from './GatePassDialog.vue'
import { DISPATCH_STATUS, TRIP_STATUS, PASS_STATUS } from '@/utils/dict'
import { fmtNum, fmtTime } from '@/utils/format'
import { dispatchDepart, dispatchCancel } from '@/api/berth'
import { publicGatePass } from '@/api/auth'

const props = defineProps({
  dispatches: { type: Array, default: () => [] },
  canDepart: Boolean,
  canAssign: Boolean,
  emptyText: { type: String, default: '暂无分派车辆' }
})
const emit = defineEmits(['changed'])
const loads = reactive({})
const busy = reactive({})
const pass = ref(null)
const passVisible = ref(false)

watch(
  () => props.dispatches,
  (list) => {
    list.forEach((d) => {
      if (!loads[d.id]) loads[d.id] = { loadTonnage: undefined, loadVolume: undefined, loadPieces: undefined }
    })
  },
  { immediate: true }
)
const isMoving = (d) => d.status === 'DEPARTED' && d.trip && d.trip.status === 'IN_TRANSIT'

async function depart(d) {
  const l = loads[d.id]
  if (!(l.loadTonnage > 0)) {
    ElMessage.warning('请填写装载吨数（方数、件数可不填）')
    return
  }
  busy[d.id] = true
  try {
    pass.value = await dispatchDepart({ dispatchId: d.id, ...l })
    passVisible.value = true
    ElMessage.success(`${d.plateNo} 已出发，出门条已发送给司机`)
    emit('changed')
  } finally {
    busy[d.id] = false
  }
}
async function cancel(d) {
  await ElMessageBox.confirm(`确定取消车辆【${d.plateNo}】的分派吗？`, '提示', { type: 'warning' })
  await dispatchCancel(d.id)
  ElMessage.success('已取消')
  emit('changed')
}
async function showPass(d) {
  pass.value = await publicGatePass(d.passToken)
  passVisible.value = true
}
</script>

<style scoped lang="scss">
.cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 12px; }
.d-card {
  border: 1px solid var(--ght-line); border-radius: 14px; padding: 12px 14px; background: #fff;
  transition: box-shadow 0.2s;
  &:hover { box-shadow: var(--ght-shadow); }
  &.WAITING { border-left: 4px solid var(--ght-working); }
  &.DEPARTED { border-left: 4px solid var(--ght-transit); }
}
.d-top { display: flex; align-items: center; gap: 10px; }
.truck-wrap { position: relative; width: 104px; flex-shrink: 0; }
.road { position: absolute; left: 0; right: 0; bottom: 2px; height: 2px; background: repeating-linear-gradient(90deg, #c8d2e2 0 10px, transparent 10px 18px); animation: roadmove 0.6s linear infinite; }
@keyframes roadmove { to { background-position: -18px 0; } }
.d-meta { flex: 1; min-width: 0; }
.plate-line { display: flex; align-items: center; justify-content: space-between; gap: 6px; }
.plate { font-weight: 800; font-size: 16px; letter-spacing: 0.5px; white-space: nowrap; }
.one-line { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.small { font-size: 12px; }
.cargo-line { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--ght-ink-2); margin: 8px 0; }
.loads { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px;
  .star { color: var(--el-color-danger); font-style: normal; margin-right: 2px; }
  label { display: flex; flex-direction: column; gap: 4px; font-size: 12px; color: var(--ght-ink-3); } :deep(.el-input-number) { width: 100%; } }
.d-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 10px; gap: 8px; }
.trip { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; div { background: #f6f8fc; border-radius: 10px; padding: 6px 10px; } b { font-size: 17px; } small { color: var(--ght-ink-3); margin-left: 3px; } }
.list-enter-active { transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1); }
.list-enter-from { opacity: 0; transform: translateY(10px) scale(0.98); }
</style>
