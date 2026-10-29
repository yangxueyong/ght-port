<template>
  <div class="ship-load-list">
    <EmptyState v-if="!loads.length" :text="emptyText" />
    <transition-group name="list" tag="div" class="cards">
      <div v-for="l in loads" :key="l.id" class="l-card" :class="l.status">
        <div class="l-top">
          <div class="ship-wrap">
            <ShipSvg :type="l.shipType" :length="l.shipLength" :color="l.hullColor || '#1f6fd1'" :loaded="l.status === 'LOADED'" :width="110" />
          </div>
          <div class="l-meta">
            <div class="name-line">
              <span class="ship-name">{{ l.shipName }}</span>
              <StatusTag :dict="SHIP_LOAD_STATUS" :value="l.status" :pulse="l.status === 'WAITING'" />
            </div>
            <div class="muted small one-line" :title="l.companyName">{{ l.companyName }}</div>
            <div class="muted small one-line">{{ l.shipTypeDesc }}</div>
          </div>
        </div>
        <div class="cargo-line"><el-icon><Box /></el-icon>{{ l.cargoName }}</div>

        <template v-if="l.status === 'WAITING'">
          <div class="loads">
            <label><span><i class="star">*</i>装船吨数</span><el-input-number v-model="form[l.id].loadTonnage" :min="0" :precision="3" :controls="false" size="small" placeholder="请输入" /></label>
            <label>装船方数<el-input-number v-model="form[l.id].loadVolume" :min="0" :precision="3" :controls="false" size="small" placeholder="选填" /></label>
            <label>装船件数<el-input-number v-model="form[l.id].loadPieces" :min="0" :controls="false" size="small" placeholder="选填" /></label>
          </div>
          <div class="l-actions">
            <el-button v-if="canEdit" link type="info" size="small" @click="cancel(l)">取消分派</el-button>
            <el-button v-if="canEdit" type="primary" size="small" icon="Finished" :loading="busy[l.id]" @click="confirm(l)">装船完成</el-button>
          </div>
        </template>
        <template v-else>
          <div class="trip">
            <div><b class="num">{{ fmtNum(l.loadTonnage, 3) }}</b><small>吨</small></div>
            <div><b class="num">{{ fmtNum(l.loadVolume, 3) }}</b><small>方</small></div>
            <div><b class="num">{{ l.loadPieces || 0 }}</b><small>件</small></div>
          </div>
          <div class="l-actions"><span class="muted small">装船于 {{ fmtTime(l.loadTime, 'MM-DD HH:mm') }}</span></div>
        </template>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ShipSvg from '@/components/svg/ShipSvg.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { SHIP_LOAD_STATUS } from '@/utils/dict'
import { fmtNum, fmtTime } from '@/utils/format'
import { shipLoadCancel, shipLoadConfirm } from '@/api/berth'

const props = defineProps({
  loads: { type: Array, default: () => [] },
  canEdit: Boolean,
  emptyText: { type: String, default: '暂无装船船只' }
})
const emit = defineEmits(['changed'])
const form = reactive({})
const busy = reactive({})

watch(
  () => props.loads,
  (list) => list.forEach((l) => !form[l.id] && (form[l.id] = { loadTonnage: undefined, loadVolume: undefined, loadPieces: undefined })),
  { immediate: true }
)

async function confirm(l) {
  const f = form[l.id]
  if (!(f.loadTonnage > 0)) {
    ElMessage.warning('请填写装船吨数（方数、件数可不填）')
    return
  }
  busy[l.id] = true
  try {
    await shipLoadConfirm({ id: l.id, ...f })
    ElMessage.success(`${l.shipName} 装船完成`)
    emit('changed')
  } finally {
    busy[l.id] = false
  }
}
async function cancel(l) {
  await ElMessageBox.confirm(`确定取消船只【${l.shipName}】的装船分派吗？`, '提示', { type: 'warning' })
  await shipLoadCancel(l.id)
  ElMessage.success('已取消')
  emit('changed')
}
</script>

<style scoped lang="scss">
.cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 12px; }
.l-card {
  border: 1px solid var(--ght-line); border-radius: 14px; padding: 12px 14px; background: #fff; transition: box-shadow 0.2s;
  &:hover { box-shadow: var(--ght-shadow); }
  &.WAITING { border-left: 4px solid var(--ght-working); }
  &.LOADED { border-left: 4px solid var(--ght-idle); }
}
.l-top { display: flex; align-items: center; gap: 10px; }
.ship-wrap { width: 110px; flex-shrink: 0; padding: 4px 0; border-radius: 10px; background: linear-gradient(180deg, #eef6ff 0 55%, #d7e9fb 55%); }
.l-meta { flex: 1; min-width: 0; }
.name-line { display: flex; align-items: center; justify-content: space-between; gap: 6px; }
.ship-name { font-weight: 800; font-size: 15px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.one-line { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.small { font-size: 12px; }
.cargo-line { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--ght-ink-2); margin: 8px 0; }
.loads { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px;
  .star { color: var(--el-color-danger); font-style: normal; margin-right: 2px; }
  label { display: flex; flex-direction: column; gap: 4px; font-size: 12px; color: var(--ght-ink-3); } :deep(.el-input-number) { width: 100%; } }
.l-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 10px; gap: 8px; }
.trip { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; div { background: #f6f8fc; border-radius: 10px; padding: 6px 10px; } b { font-size: 17px; } small { color: var(--ght-ink-3); margin-left: 3px; } }
.list-enter-active { transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1); }
.list-enter-from { opacity: 0; transform: translateY(10px) scale(0.98); }
</style>
