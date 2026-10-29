<template>
  <div class="ght-page">
    <PageHeader title="船只管理" icon="UserFilled" subtitle="按船运公司管理船只，统计累计卸货、装货量，查看正在作业的船只及装卸记录">
      <el-input v-model="query.keyword" placeholder="搜索船运公司/联系人" prefix-icon="Search" clearable style="width: 220px" @change="search" />
      <ViewToggle v-model="view" storage-key="shipper" />
      <el-button v-perm="'shipper:edit'" type="primary" icon="Plus" @click="openShipper()">新增船运公司</el-button>
    </PageHeader>

    <div v-loading="loading">
      <div v-if="view === 'card'" class="card-grid">
        <div v-for="s in list" :key="s.id" class="s-card hover-lift">
          <div class="harbor" @click="openDetail(s)">
            <div class="water" />
            <div class="fleet-row">
              <div v-for="(sh, i) in s.ships.slice(0, 3)" :key="sh.id" class="boat" :class="{ working: sh.working }" :style="{ animationDelay: i * -1.2 + 's' }" :title="sh.shipName">
                <ShipSvg :type="sh.shipType" :length="sh.shipLength" :color="sh.hullColor || '#1f6fd1'" :width="104" />
              </div>
              <div v-if="!s.ships.length" class="no-ship muted">还没有船只</div>
            </div>
            <div v-if="s.workingShipCount" class="live"><i class="status-dot pulse" style="background: #fff; color: #fff" />{{ s.workingShipCount }} 艘作业中</div>
          </div>
          <div class="s-body">
            <div class="s-head">
              <div class="s-avatar">{{ (s.shortName || s.companyName).slice(0, 2) }}</div>
              <div class="flex-1">
                <div class="s-name" @click="openDetail(s)">{{ s.companyName }}</div>
                <div class="muted small">{{ s.contactName || '—' }} · {{ s.contactPhone || '—' }}</div>
              </div>
            </div>
            <div class="s-stats">
              <div><b class="num">{{ s.shipCount }}</b><span>船只</span></div>
              <div><b class="num u">{{ fmtNum(s.unloadTonnage, 1) }}</b><span>卸货吨</span></div>
              <div><b class="num u">{{ fmtNum(s.unloadVolume, 1) }}</b><span>卸货方</span></div>
              <div><b class="num l">{{ fmtNum(s.loadTonnage, 1) }}</b><span>装货吨</span></div>
              <div><b class="num l">{{ fmtNum(s.loadVolume, 1) }}</b><span>装货方</span></div>
            </div>
            <div class="s-actions">
              <el-button size="small" type="primary" plain @click="openDetail(s)">详情</el-button>
              <el-button v-perm="'shipper:edit'" size="small" icon="Plus" @click="openShip(s)">添加船只</el-button>
              <el-button v-perm="'shipper:edit'" size="small" @click="openShipper(s)">编辑</el-button>
              <el-button v-perm="'shipper:edit'" size="small" type="danger" text @click="removeShipper(s)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="ght-card table-wrap">
        <el-table :data="list">
          <el-table-column prop="companyName" label="船运公司名称" min-width="200">
            <template #default="{ row }"><b class="link" @click="openDetail(row)">{{ row.companyName }}</b></template>
          </el-table-column>
          <el-table-column prop="contactName" label="联系人" width="100" />
          <el-table-column prop="contactPhone" label="联系电话" width="130" />
          <el-table-column prop="shipCount" label="船只数" width="80" />
          <el-table-column label="作业中" width="80"><template #default="{ row }"><span :class="{ w: row.workingShipCount }">{{ row.workingShipCount }}</span></template></el-table-column>
          <el-table-column label="累计卸货 吨/方" min-width="150"><template #default="{ row }">{{ fmtNum(row.unloadTonnage) }} / {{ fmtNum(row.unloadVolume) }}</template></el-table-column>
          <el-table-column label="累计装货 吨/方" min-width="150"><template #default="{ row }">{{ fmtNum(row.loadTonnage) }} / {{ fmtNum(row.loadVolume) }}</template></el-table-column>
          <el-table-column label="操作" width="210" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button v-perm="'shipper:edit'" link type="primary" @click="openShip(row)">添加船只</el-button>
              <el-button v-perm="'shipper:edit'" link @click="openShipper(row)">编辑</el-button>
              <el-button v-perm="'shipper:edit'" link type="danger" @click="removeShipper(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <EmptyState v-if="!list.length && !loading" text="暂无船运公司" />
      <el-pagination v-if="total > query.pageSize" class="pager" background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="load" />
    </div>

    <ShipperFormDialog v-model="shipperVisible" :data="shipperData" @saved="load" />
    <ShipFormDialog v-model="shipVisible" :data="shipData" :company-id="shipOwner?.id" :company-name="shipOwner?.companyName" @saved="load" />
    <el-drawer v-model="detailVisible" size="960px" :title="detailTitle" destroy-on-close>
      <ShipperDetailPanel :company-id="detailId" @changed="load" />
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import ViewToggle from '@/components/common/ViewToggle.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import ShipSvg from '@/components/svg/ShipSvg.vue'
import ShipperFormDialog from './components/ShipperFormDialog.vue'
import ShipFormDialog from './components/ShipFormDialog.vue'
import ShipperDetailPanel from './components/ShipperDetailPanel.vue'
import { fmtNum } from '@/utils/format'
import { shipperPage, shipperDelete } from '@/api/shipper'

const view = ref('card')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 12, keyword: '' })
const shipperVisible = ref(false)
const shipperData = ref(null)
const shipVisible = ref(false)
const shipData = ref(null)
const shipOwner = ref(null)
const detailVisible = ref(false)
const detailId = ref(null)
const detailTitle = ref('')

async function load() {
  loading.value = true
  try {
    const res = await shipperPage(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}
function search() {
  query.pageNum = 1
  load()
}
function openShipper(s) {
  shipperData.value = s ? { ...s } : null
  shipperVisible.value = true
}
function openShip(owner) {
  shipOwner.value = owner
  shipData.value = null
  shipVisible.value = true
}
function openDetail(s) {
  detailId.value = s.id
  detailTitle.value = s.companyName
  detailVisible.value = true
}
async function removeShipper(s) {
  await ElMessageBox.confirm(`确定删除船运公司【${s.companyName}】及其全部船只吗？`, '删除确认', { type: 'warning' })
  await shipperDelete(s.id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

<style scoped lang="scss">
.s-card { background: #fff; border: 1px solid var(--ght-line); border-radius: 18px; overflow: hidden; }
.harbor { position: relative; height: 118px; cursor: pointer; overflow: hidden; background: linear-gradient(180deg, #eef6ff 0 52%, #d7e9fb 52%); }
.water { position: absolute; left: 0; right: 0; bottom: 0; height: 48%; background: repeating-linear-gradient(90deg, transparent 0 18px, rgba(255, 255, 255, 0.5) 18px 26px); animation: water 3s linear infinite; opacity: 0.6; }
@keyframes water { to { background-position: 44px 0; } }
.fleet-row { position: relative; display: flex; justify-content: center; align-items: flex-end; height: 100%; gap: 4px; padding-bottom: 14px; }
.boat { animation: sway 4s ease-in-out infinite; opacity: 0.85; &.working { opacity: 1; filter: drop-shadow(0 4px 6px rgba(245, 159, 0, 0.45)); } }
@keyframes sway { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(2px); } }
.no-ship { align-self: center; font-size: 13px; }
.live { position: absolute; right: 12px; top: 10px; font-size: 12px; color: #fff; padding: 3px 10px; border-radius: 999px; background: var(--ght-working); box-shadow: 0 4px 10px -4px var(--ght-working); }
.s-body { padding: 14px 16px 16px; }
.s-head { display: flex; align-items: center; gap: 12px; }
.s-avatar { width: 42px; height: 42px; border-radius: 13px; display: grid; place-items: center; font-weight: 800; color: #fff; background: linear-gradient(135deg, #22c3e6, #0a64b0); flex-shrink: 0; }
.s-name { font-weight: 700; font-size: 16px; cursor: pointer; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; &:hover { color: var(--ght-blue); } }
.small { font-size: 12px; }
.flex-1 { flex: 1; min-width: 0; }
.s-stats { display: grid; grid-template-columns: 0.7fr repeat(4, 1fr); gap: 6px; margin-top: 12px;
  div { background: #f6f8fc; border-radius: 10px; padding: 8px 4px; display: flex; flex-direction: column; align-items: center; min-width: 0; }
  b { font-size: 15px; white-space: nowrap; &.u { color: #1f7ae0; } &.l { color: #7c3aed; } } span { font-size: 11px; color: var(--ght-ink-3); } }
.s-actions { display: flex; gap: 6px; margin-top: 12px; .el-button { margin: 0; } }
.table-wrap { padding: 6px; }
.link { cursor: pointer; &:hover { color: var(--ght-blue); } }
.w { color: var(--ght-working); font-weight: 700; }
.pager { margin-top: 18px; justify-content: flex-end; }
</style>
