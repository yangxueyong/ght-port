<template>
  <div v-loading="loading" class="panel">
    <template v-if="d">
      <div class="hero">
        <div class="hero-scene">
          <div class="waves" />
          <div v-for="(sh, i) in workingShips.slice(0, 3)" :key="sh.id" class="mover" :style="{ animationDelay: `${i * -4}s`, bottom: `${6 + (i % 2) * 16}px` }">
            <ShipSvg :type="sh.shipType" :length="sh.shipLength" :color="sh.hullColor || '#1f6fd1'" sailing :width="120" />
          </div>
        </div>
        <div class="hero-info">
          <div class="avatar">{{ (d.company.shortName || d.company.companyName).slice(0, 2) }}</div>
          <div class="flex-1">
            <div class="name">{{ d.company.companyName }}</div>
            <div class="sub">{{ d.company.contactName || '—' }} · {{ d.company.contactPhone || '—' }} · {{ d.company.address || '—' }}</div>
          </div>
        </div>
      </div>

      <div class="stats">
        <div><span>船只总数</span><b class="num">{{ d.company.shipCount }}</b></div>
        <div><span>正在作业</span><b class="num w">{{ d.company.workingShipCount }}</b></div>
        <div><span>累计卸货吨数</span><b class="num u">{{ fmtNum(d.company.unloadTonnage) }}</b></div>
        <div><span>累计卸货方数</span><b class="num u">{{ fmtNum(d.company.unloadVolume) }}</b></div>
        <div><span>累计装货吨数</span><b class="num l">{{ fmtNum(d.company.loadTonnage) }}</b></div>
        <div><span>累计装货方数</span><b class="num l">{{ fmtNum(d.company.loadVolume) }}</b></div>
      </div>

      <el-tabs v-model="tab" class="tabs">
        <el-tab-pane :label="`船队 (${d.ships.length})`" name="fleet">
          <div class="flex-between mb">
            <el-radio-group v-model="shipFilter" size="small">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button value="WORKING">作业中</el-radio-button>
              <el-radio-button value="IDLE">空闲</el-radio-button>
            </el-radio-group>
            <el-button v-if="canEdit" type="primary" size="small" icon="Plus" @click="editShip()">添加船只</el-button>
          </div>
          <EmptyState v-if="!fleet.length" text="暂无船只" />
          <div class="fleet">
            <div v-for="sh in fleet" :key="sh.id" class="v-card" :class="{ working: sh.working }">
              <div class="v-scene">
                <ShipSvg :type="sh.shipType" :length="sh.shipLength" :color="sh.hullColor || '#1f6fd1'" :sailing="!!sh.working" :width="190" />
              </div>
              <div class="v-info">
                <div class="flex-between">
                  <span class="plate">{{ sh.shipName }}</span>
                  <span class="tag" :class="{ on: sh.working }"><i class="status-dot" :class="{ pulse: sh.working }" />{{ sh.working ? workDesc(sh.working) : '空闲' }}</span>
                </div>
                <div class="muted small">{{ sh.shipTypeDesc }} · {{ sh.shipLength || '-' }}米 · {{ sh.deadweight || '-' }}吨 · {{ sh.captainName || '' }} {{ sh.captainPhone || '' }}</div>
                <div v-if="sh.working" class="cur">
                  <el-icon><Location /></el-icon><span class="ellipsis">{{ sh.working.berthName }} · {{ sh.working.cargoName }}</span>
                  <span class="muted">{{ fromNow(sh.working.startTime) }}开始</span>
                </div>
                <div class="mini">
                  <span>累计卸货 <b class="num u">{{ fmtNum(sh.unloadTonnage, 1) }}</b> 吨</span>
                  <span>累计装货 <b class="num l">{{ fmtNum(sh.loadTonnage, 1) }}</b> 吨</span>
                </div>
                <div class="v-actions">
                  <el-button v-if="sh.working && canComplete" type="success" size="small" plain icon="CircleCheck" @click="complete(sh)">完成运输</el-button>
                  <span class="flex-1" />
                  <el-button v-if="canEdit" link type="primary" size="small" @click="editShip(sh)">编辑</el-button>
                  <el-button v-if="canEdit" link type="danger" size="small" @click="removeShip(sh)">删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="`作业中船只 (${workingShips.length})`" name="working">
          <EmptyState v-if="!workingShips.length" text="当前没有正在作业的船只" />
          <div v-for="sh in workingShips" :key="sh.id" class="trip-row">
            <ShipSvg :type="sh.shipType" :length="sh.shipLength" :color="sh.hullColor || '#1f6fd1'" sailing :width="110" />
            <div class="flex-1">
              <div><b>{{ sh.shipName }}</b> <span class="muted">· {{ sh.shipTypeDesc }}</span></div>
              <div class="small">{{ sh.working.berthName }} · {{ sh.working.cargoName }}</div>
            </div>
            <span class="dir" :style="dirStyle(sh.working.direction)">{{ workDesc(sh.working) }}</span>
            <div class="muted small time">{{ fmtTime(sh.working.startTime, 'MM-DD HH:mm') }}</div>
            <el-button v-if="canComplete" type="success" size="small" plain @click="complete(sh)">完成运输</el-button>
          </div>
        </el-tab-pane>

        <el-tab-pane label="卸货记录" name="unload" lazy>
          <el-table :data="unload.records" size="small" v-loading="unloadLoading">
            <el-table-column label="到货时间" width="120"><template #default="{ row }">{{ fmtTime(row.arriveTime, 'MM-DD HH:mm') }}</template></el-table-column>
            <el-table-column prop="berthName" label="泊位" width="120" show-overflow-tooltip />
            <el-table-column label="货物" min-width="160" show-overflow-tooltip><template #default="{ row }">{{ row.cargoName }} <span class="muted">（{{ row.cargoTypeName }}·{{ labelOf(PACK_TYPES, row.packType) }}）</span></template></el-table-column>
            <el-table-column label="船只" min-width="120" show-overflow-tooltip><template #default="{ row }">{{ shipNames(row) }}</template></el-table-column>
            <el-table-column label="吨 / 方" width="150"><template #default="{ row }">{{ fmtNum(row.tonnage, 3) }} / {{ fmtNum(row.volume, 3) }}</template></el-table-column>
            <el-table-column label="已运出" width="100"><template #default="{ row }">{{ fmtNum(row.loadedTonnage, 3) }}吨</template></el-table-column>
            <el-table-column label="状态" width="90"><template #default="{ row }"><StatusTag :dict="CARGO_STATUS" :value="row.status" /></template></el-table-column>
          </el-table>
          <el-pagination class="pager" small layout="prev, pager, next, total" :total="unload.total || 0" :page-size="10" v-model:current-page="unloadPage" @current-change="loadUnload" />
        </el-tab-pane>

        <el-tab-pane label="装货记录" name="load" lazy>
          <el-table :data="loadRec.records" size="small" v-loading="loadLoading">
            <el-table-column label="装船时间" width="120"><template #default="{ row }">{{ row.loadTime ? fmtTime(row.loadTime, 'MM-DD HH:mm') : '—' }}</template></el-table-column>
            <el-table-column prop="berthName" label="泊位" width="120" show-overflow-tooltip />
            <el-table-column prop="shipName" label="船只" width="120" show-overflow-tooltip />
            <el-table-column label="货物" min-width="160" show-overflow-tooltip><template #default="{ row }">{{ row.cargoName }} <span class="muted">（{{ row.cargoTypeName }}）</span></template></el-table-column>
            <el-table-column label="装船 吨 / 方" width="160"><template #default="{ row }">{{ fmtNum(row.loadTonnage, 3) }} / {{ fmtNum(row.loadVolume, 3) }}</template></el-table-column>
            <el-table-column label="状态" width="90"><template #default="{ row }"><StatusTag :dict="SHIP_LOAD_STATUS" :value="row.status" /></template></el-table-column>
          </el-table>
          <el-pagination class="pager" small layout="prev, pager, next, total" :total="loadRec.total || 0" :page-size="10" v-model:current-page="loadPage" @current-change="loadLoad" />
        </el-tab-pane>
      </el-tabs>
    </template>

    <el-dialog v-model="loadDialog.visible" title="完成装船" width="440px" append-to-body>
      <div class="load-tip">船只【{{ loadDialog.ship?.shipName }}】正在 {{ loadDialog.ship?.working?.berthName }} 装货（{{ loadDialog.ship?.working?.cargoName }}），请填写装船量：</div>
      <el-form label-width="84px">
        <el-form-item label="装船吨数" required><el-input-number v-model="loadDialog.loadTonnage" :min="0" :precision="3" controls-position="right" style="width: 200px" /><span class="unit">吨</span></el-form-item>
        <el-form-item label="装船方数"><el-input-number v-model="loadDialog.loadVolume" :min="0" :precision="3" :controls="false" placeholder="选填" style="width: 200px" /><span class="unit">方</span></el-form-item>
        <el-form-item label="装船件数"><el-input-number v-model="loadDialog.loadPieces" :min="0" :controls="false" placeholder="选填" style="width: 200px" /><span class="unit">件</span></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="loadDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="loadDialog.saving" @click="submitLoad">完成装船</el-button>
      </template>
    </el-dialog>
    <ShipFormDialog v-model="shipDialog" :data="shipData" :company-id="d?.company.id" :company-name="d?.company.companyName" @saved="refresh" />
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ShipSvg from '@/components/svg/ShipSvg.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import ShipFormDialog from './ShipFormDialog.vue'
import { CARGO_DIRECTION, CARGO_STATUS, PACK_TYPES, SHIP_LOAD_STATUS, labelOf } from '@/utils/dict'
import { fmtNum, fmtTime, fromNow } from '@/utils/format'
import { shipperDetail, shipDelete, shipComplete } from '@/api/shipper'
import { cargoPage, shipLoadPage } from '@/api/berth'
import { useUserStore } from '@/stores/user'

const props = defineProps({ companyId: String })
const emit = defineEmits(['changed'])
const store = useUserStore()
const canEdit = computed(() => store.hasPerm('shipper:edit'))
const canComplete = computed(() => store.hasPerm('shipper:edit') || store.hasPerm('berth:work:edit'))
const loadDialog = reactive({ visible: false, saving: false, ship: null, loadTonnage: undefined, loadVolume: undefined, loadPieces: undefined })
const d = ref(null)
const loading = ref(false)
const tab = ref('fleet')
const shipFilter = ref('')
const shipDialog = ref(false)
const shipData = ref(null)
const unload = ref({ records: [], total: 0 })
const unloadPage = ref(1)
const unloadLoading = ref(false)
const loadRec = ref({ records: [], total: 0 })
const loadPage = ref(1)
const loadLoading = ref(false)

const workingShips = computed(() => (d.value?.ships || []).filter((s) => s.working))
const fleet = computed(() =>
  (d.value?.ships || []).filter((s) => !shipFilter.value || (shipFilter.value === 'WORKING' ? s.working : !s.working))
)
const workDesc = (w) => (w.direction === 'LOAD' ? '正在装货' : '正在卸货')
const dirStyle = (dir) => {
  const c = CARGO_DIRECTION[dir] || CARGO_DIRECTION.UNLOAD
  return { color: c.color, background: c.bg }
}
const shipNames = (row) => (row.ships || []).filter((s) => s.companyId === props.companyId).map((s) => s.shipName).join('、') || '-'

async function load() {
  loading.value = true
  try {
    d.value = await shipperDetail(props.companyId)
    unloadPage.value = 1
    loadPage.value = 1
    loadUnload()
    loadLoad()
  } finally {
    loading.value = false
  }
}
async function loadUnload() {
  unloadLoading.value = true
  try {
    unload.value = await cargoPage({ companyId: props.companyId, direction: 'UNLOAD', pageNum: unloadPage.value, pageSize: 10 })
  } finally {
    unloadLoading.value = false
  }
}
async function loadLoad() {
  loadLoading.value = true
  try {
    loadRec.value = await shipLoadPage({ companyId: props.companyId, statuses: ['WAITING', 'LOADED'], pageNum: loadPage.value, pageSize: 10 })
  } finally {
    loadLoading.value = false
  }
}
async function refresh() {
  await load()
  emit('changed')
}
function editShip(sh) {
  shipData.value = sh ? { ...sh } : null
  shipDialog.value = true
}
/** 完成运输：卸货船只直接确认；装货船只需填写装船量 */
async function complete(sh) {
  if (sh.working?.direction === 'LOAD') {
    Object.assign(loadDialog, { visible: true, saving: false, ship: sh, loadTonnage: undefined, loadVolume: undefined, loadPieces: undefined })
    return
  }
  await ElMessageBox.confirm(`确认船只【${sh.shipName}】已在 ${sh.working.berthName} 完成卸货（${sh.working.cargoName}）？`, '完成运输', { type: 'info' })
  await shipComplete({ shipId: sh.id })
  ElMessage.success('已设置为完成运输')
  refresh()
}
async function submitLoad() {
  if (!(loadDialog.loadTonnage > 0)) {
    ElMessage.warning('请填写装船吨数（方数、件数可不填）')
    return
  }
  loadDialog.saving = true
  try {
    const { ship, loadTonnage, loadVolume, loadPieces } = loadDialog
    await shipComplete({ shipId: ship.id, loadTonnage, loadVolume, loadPieces })
    ElMessage.success(`${ship.shipName} 装船完成`)
    loadDialog.visible = false
    refresh()
  } finally {
    loadDialog.saving = false
  }
}
async function removeShip(sh) {
  await ElMessageBox.confirm(`确定删除船只【${sh.shipName}】吗？`, '删除确认', { type: 'warning' })
  await shipDelete(sh.id)
  ElMessage.success('已删除')
  refresh()
}

watch(() => props.companyId, (v) => v && load(), { immediate: true })
</script>

<style scoped lang="scss">
.hero { position: relative; border-radius: 16px; overflow: hidden; color: #fff; background: linear-gradient(135deg, #0a64b0, #22a6e6); }
.hero-scene { position: relative; height: 96px; overflow: hidden; }
.waves { position: absolute; left: 0; right: 0; bottom: 0; height: 40px; background: repeating-linear-gradient(90deg, rgba(255, 255, 255, 0.18) 0 18px, transparent 18px 34px); animation: wave 1.6s linear infinite; }
@keyframes wave { to { background-position: 34px 0; } }
.mover { position: absolute; left: -140px; animation: sail 16s linear infinite; }
@keyframes sail { to { transform: translateX(calc(100vw)); } }
.hero-info { position: relative; display: flex; align-items: center; gap: 14px; padding: 4px 20px 18px; }
.avatar { width: 52px; height: 52px; border-radius: 16px; display: grid; place-items: center; font-weight: 800; font-size: 17px; background: rgba(255, 255, 255, 0.2); border: 1px solid rgba(255, 255, 255, 0.35); }
.name { font-size: 20px; font-weight: 700; }
.sub { font-size: 13px; opacity: 0.85; margin-top: 4px; }
.flex-1 { flex: 1; min-width: 0; }
.stats { display: grid; grid-template-columns: repeat(6, 1fr); gap: 10px; margin-top: 14px;
  div { background: #f6f8fc; border-radius: 12px; padding: 10px 12px; display: flex; flex-direction: column; gap: 2px; }
  span { font-size: 12px; color: var(--ght-ink-3); } b { font-size: 20px; } }
.w { color: var(--ght-working); }
.u { color: #1f7ae0; }
.l { color: #7c3aed; }
.tabs { margin-top: 8px; }
.mb { margin-bottom: 12px; }
.small { font-size: 12px; }
.fleet { display: grid; grid-template-columns: repeat(auto-fill, minmax(290px, 1fr)); gap: 12px; }
.v-card { border: 1px solid var(--ght-line); border-radius: 14px; overflow: hidden; background: #fff; transition: box-shadow 0.2s;
  &:hover { box-shadow: var(--ght-shadow); }
  &.working { border-color: #fbd38a; .v-scene { background: linear-gradient(180deg, #fff6e5 0 58%, #ffe3b0 58%); } } }
.v-scene { height: 90px; display: flex; align-items: flex-end; justify-content: center; padding-bottom: 6px; background: linear-gradient(180deg, #eef6ff 0 58%, #d7e9fb 58%); }
.v-info { padding: 10px 12px 12px; }
.plate { font-weight: 800; font-size: 16px; white-space: nowrap; }
.tag { display: inline-flex; align-items: center; gap: 5px; font-size: 12px; padding: 2px 8px; border-radius: 999px; color: #12b886; background: #e6f8f1;
  .status-dot { background: currentColor; } &.on { color: #f59f00; background: #fff4de; } }
.cur { display: flex; align-items: center; gap: 6px; margin-top: 8px; padding: 6px 8px; border-radius: 8px; background: #fff8eb; font-size: 12px; }
.ellipsis { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.mini { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-top: 10px; font-size: 12px; color: var(--ght-ink-3);
  span { white-space: nowrap; padding: 5px 8px; border-radius: 8px; background: #f6f8fc; }
  b { font-size: 14px; margin: 0 2px; } }
.v-actions { display: flex; align-items: center; gap: 4px; margin-top: 10px; min-height: 24px; }
.trip-row { display: flex; align-items: center; gap: 14px; padding: 10px 12px; border: 1px solid var(--ght-line); border-radius: 12px; margin-bottom: 10px;
  .time { width: 80px; text-align: right; } }
.dir { font-size: 12px; padding: 2px 10px; border-radius: 999px; font-weight: 600; white-space: nowrap; }
.pager { margin-top: 12px; justify-content: flex-end; }
.load-tip { font-size: 13px; color: var(--ght-ink-2); margin-bottom: 14px; line-height: 1.6; }
.unit { margin-left: 8px; color: var(--ght-ink-3); }
@media (max-width: 1200px) { .stats { grid-template-columns: repeat(3, 1fr); } }
</style>
