<template>
  <el-drawer :model-value="modelValue" size="720px" :with-header="false" @update:model-value="emit('update:modelValue', $event)" @open="load">
    <div v-loading="loading" class="detail">
      <div v-if="d" class="hero" :class="d.berth.status">
        <div class="hero-bg" />
        <div class="hero-main">
          <div class="code num">{{ d.berth.berthCode }}</div>
          <div>
            <div class="name">{{ d.berth.berthName }}</div>
            <div class="sub">{{ d.portName }} · 作业时间 {{ workTime }}</div>
          </div>
          <el-icon class="close" @click="emit('update:modelValue', false)"><Close /></el-icon>
        </div>
        <div class="hero-tags">
          <StatusTag :dict="BERTH_STATUS" :value="d.berth.status" :pulse="d.berth.status !== 'IDLE'" />
          <span class="spec">长度 {{ d.berth.berthLength ?? '-' }} 米</span>
          <span class="spec">水深 {{ d.berth.waterDepth ?? '-' }} 米</span>
          <span class="spec">{{ d.berth.maxTonnage ?? '-' }} 吨级</span>
        </div>
      </div>

      <div v-if="d && !readonly && (canRegister || canWork)" class="actions">
        <div v-if="canRegister" class="act-group">
          <div class="act-label"><el-icon><Box /></el-icon>货物登记 · 分派承运/船只请在下方货物中操作</div>
          <div class="act-btns">
            <el-button v-if="has('berth:cargo:arrive')" type="primary" icon="Download" :disabled="d.berth.status === 'MAINTENANCE'" @click="openCargoForm('arrive')">泊位到货</el-button>
            <el-button v-if="has('berth:cargo:load')" type="warning" icon="Upload" :disabled="d.berth.status === 'MAINTENANCE'" @click="openCargoForm('load')">泊位装货</el-button>
          </div>
        </div>
        <div v-if="canWork" class="act-group state">
          <div class="act-label"><el-icon><Setting /></el-icon>泊位状态</div>
          <div class="act-btns">
            <el-button v-if="d.berth.status === 'WORKING'" type="success" plain icon="CircleCheck" @click="finish">完成作业</el-button>
            <el-button v-if="d.berth.status !== 'MAINTENANCE'" type="danger" plain icon="Tools" @click="toMaintenance">转为维修</el-button>
            <el-button v-else type="success" plain icon="RefreshRight" @click="restore">恢复使用</el-button>
          </div>
        </div>
      </div>
      <div v-else-if="d && readonly" class="readonly-tip"><el-icon><View /></el-icon>只读查看 · 如需到货、装货或分派，请前往“泊位管理”</div>

      <div v-if="d" class="kpis">
        <div><b class="num">{{ d.cargos.length }}</b><span>作业货物批次</span></div>
        <div><b class="num" style="color: var(--ght-working)">{{ d.berth.waitingCount }}</b><span>待装载车辆</span></div>
        <div><b class="num" style="color: var(--ght-transit)">{{ d.berth.inTransitCount }}</b><span>运输中车辆</span></div>
        <div><b class="num" style="color: #7c3aed">{{ waitingShips }}</b><span>待装船船只</span></div>
      </div>

      <el-tabs v-if="d" v-model="tab" class="tabs">
        <el-tab-pane label="货物内容" name="cargo">
          <div class="flex-between cargo-bar">
            <el-segmented v-model="cargoScope" :options="[{ label: `当前作业 (${d.cargos.length})`, value: 'active' }, { label: '历史货物', value: 'history' }]" size="small" @change="onScope" />
            <span class="muted small">按到货时间倒序 · 点击货物查看承运商与车辆 / 装船船只</span>
          </div>
          <template v-if="cargoScope === 'active'">
            <EmptyState v-if="!d.cargos.length" :text="d.berth.status === 'MAINTENANCE' ? '泊位维修中' : '暂无作业货物，可点击“泊位到货”或“泊位装货”录入'" />
            <CargoCard v-for="c in d.cargos" :key="c.id" :cargo="c" :open="openId === c.id" :stamp="stamp" :assignable="assignable(c)" @toggle="toggle(c.id)" @assign="openAssign(c)" />
          </template>
          <template v-else>
            <div v-loading="historyLoading">
              <EmptyState v-if="!history.records.length && !historyLoading" text="暂无历史货物" />
              <CargoCard v-for="c in history.records" :key="c.id" :cargo="c" :open="openId === c.id" :stamp="stamp" @toggle="toggle(c.id)" />
              <el-pagination v-if="history.total > 10" class="pager" small layout="prev, pager, next, total" :total="history.total" :page-size="10" v-model:current-page="historyPage" @current-change="loadHistory" />
            </div>
          </template>
        </el-tab-pane>
        <el-tab-pane :label="`接驳承运商 (${d.carriers.length})`" name="carrier">
          <EmptyState v-if="!d.carriers.length" text="尚未分派承运商" />
          <div class="carrier-chips">
            <div v-for="c in carrierStats" :key="c.id" class="carrier-chip">
              <div class="avatar">{{ c.carrierName.slice(0, 2) }}</div>
              <div class="flex-1">
                <div><b>{{ c.carrierName }}</b></div>
                <div class="muted small">待装载 {{ c.waiting }} · 运输中 {{ c.transit }} · 已完成 {{ c.done }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="`相关车辆 (${d.dispatches.length})`" name="vehicle">
          <DispatchList :dispatches="d.dispatches" :can-depart="!readonly && has('berth:dispatch:depart')" :can-assign="!readonly && has('berth:dispatch:assign')" @changed="refresh" />
        </el-tab-pane>
        <el-tab-pane v-if="loadCount || d.shipLoads?.length" :label="`装船船只 (${d.shipLoads?.length || 0})`" name="ship">
          <ShipLoadList :loads="d.shipLoads || []" :can-edit="!readonly && has('berth:cargo:load')" @changed="refresh" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <ArriveDialog v-model="arriveVisible" :berth="d?.berth" :mode="cargoFormMode" @done="refresh" />
    <DispatchDrawer v-model="dispatchVisible" :berth="d?.berth" :cargos="d?.cargos || []" :cargo-id="assignCargoId" @changed="refresh" />
    <ShipLoadDrawer v-model="shipLoadVisible" :berth="d?.berth" :cargos="d?.cargos || []" :cargo-id="assignCargoId" @changed="refresh" />
  </el-drawer>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import ArriveDialog from './ArriveDialog.vue'
import DispatchDrawer from './DispatchDrawer.vue'
import DispatchList from './DispatchList.vue'
import ShipLoadDrawer from './ShipLoadDrawer.vue'
import ShipLoadList from './ShipLoadList.vue'
import CargoCard from './CargoCard.vue'
import { BERTH_STATUS } from '@/utils/dict'
import { berthDetail, berthFinish, berthStatus, cargoPage } from '@/api/berth'
import { withConfirm } from '@/utils/confirm'
import { useUserStore } from '@/stores/user'

/** readonly：只读查看（工作台），不显示到货/装货/分派/状态等操作 */
const props = defineProps({ modelValue: Boolean, berthId: String, readonly: Boolean })
const emit = defineEmits(['update:modelValue', 'changed'])
const store = useUserStore()
const has = (p) => store.hasPerm(p)
const d = ref(null)
const loading = ref(false)
const tab = ref('cargo')
const arriveVisible = ref(false)
const dispatchVisible = ref(false)
const shipLoadVisible = ref(false)
const cargoFormMode = ref('arrive')
const cargoScope = ref('active')
const openId = ref(null)
const stamp = ref(0)
const history = ref({ records: [], total: 0 })
const historyPage = ref(1)
const historyLoading = ref(false)

const canRegister = computed(() => has('berth:cargo:arrive') || has('berth:cargo:load'))
/** 当前作业货物上的分派按钮：到货可分派承运，装货可分派船只 */
/** 货物已全部运出/装船：吨数、方数、件数中有登记的各项剩余都为 0 */
const isCargoDone = (c) => {
  const pairs = [[c.tonnage, c.loadedTonnage], [c.volume, c.loadedVolume], [c.pieces, c.loadedPieces]].filter(([t]) => Number(t) > 0)
  return pairs.length > 0 && pairs.every(([t, l]) => Number(l) >= Number(t))
}
const assignable = (c) =>
  !props.readonly && d.value?.berth.status === 'WORKING' && !isCargoDone(c) && (c.direction === 'LOAD' ? has('berth:cargo:load') : has('berth:dispatch:assign'))
const assignCargoId = ref(null)
function openAssign(c) {
  assignCargoId.value = c.id
  if (c.direction === 'LOAD') shipLoadVisible.value = true
  else dispatchVisible.value = true
}
const canWork = computed(() => has('berth:work:edit'))
const loadCount = computed(() => (d.value?.cargos || []).filter((c) => c.direction === 'LOAD').length)
const waitingShips = computed(() => (d.value?.shipLoads || []).filter((l) => l.status === 'WAITING').length)

function openCargoForm(mode) {
  cargoFormMode.value = mode
  arriveVisible.value = true
}
function toggle(id) {
  openId.value = openId.value === id ? null : id
}
function onScope() {
  openId.value = null
  if (cargoScope.value === 'history') {
    historyPage.value = 1
    loadHistory()
  }
}
async function loadHistory() {
  historyLoading.value = true
  try {
    history.value = await cargoPage({ berthId: props.berthId, status: 'COMPLETED', pageNum: historyPage.value, pageSize: 10 })
  } finally {
    historyLoading.value = false
  }
}

const workTime = computed(() => {
  const b = d.value?.berth
  return b?.workStartTime ? `${b.workStartTime.slice(0, 5)} - ${b.workEndTime.slice(0, 5)}` : '未设置'
})
const carrierStats = computed(() =>
  (d.value?.carriers || []).map((c) => {
    const list = d.value.dispatches.filter((x) => x.carrierId === c.id)
    return {
      ...c,
      waiting: list.filter((x) => x.status === 'WAITING').length,
      transit: list.filter((x) => x.trip?.status === 'IN_TRANSIT').length,
      done: list.filter((x) => x.trip?.status === 'COMPLETED').length
    }
  })
)
async function load() {
  if (!props.berthId) return
  loading.value = true
  try {
    const first = !d.value || d.value.berth.id !== props.berthId
    d.value = await berthDetail(props.berthId)
    stamp.value++
    if (first) {
      tab.value = 'cargo'
      cargoScope.value = 'active'
      openId.value = null
    }
  } finally {
    loading.value = false
  }
}
async function refresh() {
  await load()
  emit('changed')
}
async function finish() {
  const res = await withConfirm(berthFinish, { id: d.value.berth.id })
  if (res) {
    ElMessage.success('作业已完成，泊位恢复为未作业')
    refresh()
  }
}
async function toMaintenance() {
  const res = await withConfirm(berthStatus, { id: d.value.berth.id, status: 'MAINTENANCE' })
  if (res) {
    ElMessage.success('泊位已转为维修中')
    refresh()
  }
}
async function restore() {
  const res = await withConfirm(berthStatus, { id: d.value.berth.id, status: 'IDLE' })
  if (res) {
    ElMessage.success('泊位已恢复使用')
    refresh()
  }
}
</script>

<style scoped lang="scss">
.detail { min-height: 100%; }
.hero {
  position: relative; overflow: hidden; color: #fff; padding: 22px 24px 18px;
  --c1: #12b886; --c2: #0b7a5a;
  &.WORKING { --c1: #f59f00; --c2: #c25e00; }
  &.MAINTENANCE { --c1: #e5484d; --c2: #8f1f24; }
  background: linear-gradient(120deg, var(--c2), var(--c1));
}
.hero-bg {
  position: absolute; inset: 0; opacity: 0.18;
  background: repeating-linear-gradient(-45deg, #fff 0 2px, transparent 2px 14px);
}
.MAINTENANCE .hero-bg { opacity: 0.25; background: repeating-linear-gradient(-45deg, #000 0 12px, transparent 12px 24px); }
.WORKING .hero-bg { animation: slide 2s linear infinite; background-size: 200% 200%; }
@keyframes slide { to { background-position: 40px 0; } }
.hero-main { position: relative; display: flex; align-items: center; gap: 16px; }
.code { font-size: 44px; font-weight: 800; line-height: 1; padding: 6px 14px; border-radius: 14px; background: rgba(255, 255, 255, 0.18); }
.name { font-size: 20px; font-weight: 700; }
.sub { opacity: 0.85; margin-top: 4px; font-size: 13px; }
.close { margin-left: auto; font-size: 20px; cursor: pointer; opacity: 0.8; align-self: flex-start; &:hover { opacity: 1; } }
.hero-tags { position: relative; display: flex; gap: 8px; margin-top: 14px; flex-wrap: wrap; }
.spec { font-size: 12px; padding: 2px 10px; border-radius: 999px; background: rgba(255, 255, 255, 0.18); line-height: 20px; }
.actions { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 10px; padding: 14px 24px; border-bottom: 1px solid var(--ght-line); }
.act-group { padding: 8px 10px 10px; border-radius: 12px; background: #f6f8fc; border: 1px solid #eef2f8;
  &.state { background: #fbfbfd; } }
.act-label { display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--ght-ink-3); margin-bottom: 8px; }
.act-btns { display: flex; gap: 8px;
  > .el-button, > span { flex: 1; min-width: 0; margin: 0; }
  > span .el-button { width: 100%; }
  .el-button { margin: 0; padding: 8px 10px; } }
.readonly-tip { display: flex; align-items: center; gap: 6px; padding: 10px 24px; font-size: 12px; color: var(--ght-ink-3); background: #f8fafd; border-bottom: 1px solid var(--ght-line); }
.kpis { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; padding: 14px 24px 0;
  div { background: #f6f8fc; border-radius: 12px; padding: 10px 12px; display: flex; flex-direction: column; }
  b { font-size: 24px; } span { font-size: 12px; color: var(--ght-ink-3); } }
.tabs { padding: 6px 24px 24px; }
.small { font-size: 12px; }
.cargo-bar { margin-bottom: 12px; gap: 10px; flex-wrap: wrap; }
.pager { justify-content: flex-end; margin-top: 8px; }
.carrier-chips { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.carrier-chip { display: flex; align-items: center; gap: 10px; padding: 12px; border: 1px solid var(--ght-line); border-radius: 12px; }
.avatar { width: 40px; height: 40px; border-radius: 12px; display: grid; place-items: center; color: #fff; font-weight: 700; font-size: 13px; background: linear-gradient(135deg, #1f7ae0, #182d86); }
.flex-1 { flex: 1; }
</style>
