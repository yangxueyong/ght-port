<template>
  <div class="ght-page">
    <PageHeader title="承运商管理" icon="Van" subtitle="管理承运商与车队，查看正在运输的车辆与货物，统计承运商运输情况">
      <el-input v-model="query.keyword" placeholder="搜索承运商/联系人/电话" prefix-icon="Search" clearable style="width: 240px" @change="search" />
      <el-segmented v-model="transitFilter" :options="[{ label: '全部', value: false }, { label: '运输中', value: true }]" @change="search" />
      <ViewToggle v-model="view" storage-key="carrier" />
      <el-button v-perm="'carrier:edit'" type="primary" icon="Plus" @click="openForm()">新增承运商</el-button>
    </PageHeader>

    <div v-loading="loading">
      <div v-if="view === 'card'" class="card-grid">
        <div v-for="c in list" :key="c.id" class="c-card hover-lift" :style="{ '--brand': c.brandColor || '#1f7ae0' }">
          <div class="c-scene" @click="openDetail(c)">
            <div class="sky" />
            <div class="road"><i /></div>
            <div v-for="i in Math.min(4, c.vehicleCount)" :key="i" class="truck" :class="{ go: i <= c.inTransitVehicleCount }" :style="truckStyle(i, c)">
              <TruckSvg :type="truckTypes[(i - 1) % truckTypes.length]" :length="8 + i" :color="c.brandColor || '#1f7ae0'" :moving="i <= c.inTransitVehicleCount" :width="78" />
            </div>
            <div v-if="!c.vehicleCount" class="no-truck">暂无车辆</div>
            <div v-if="c.inTransitVehicleCount" class="live"><i class="status-dot pulse" style="background: #fff; color: #fff" />{{ c.inTransitVehicleCount }} 辆运输中</div>
          </div>
          <div class="c-body">
            <div class="flex-between">
              <div class="c-name" @click="openDetail(c)">{{ c.carrierName }}</div>
              <StatusTag :dict="ENABLE_STATUS" :value="c.status" />
            </div>
            <div class="muted small c-contact"><el-icon><User /></el-icon>{{ c.contactName }} · {{ c.contactPhone }}</div>
            <div class="c-stats">
              <div><b class="num">{{ c.vehicleCount }}</b><span>车辆</span></div>
              <div><b class="num">{{ c.stat.tripCount }}</b><span>运输趟数</span></div>
              <div><b class="num">{{ fmtNum(c.stat.totalTonnage, 1) }}</b><span>累计吨</span></div>
              <div><b class="num">{{ fmtNum(c.stat.totalVolume, 1) }}</b><span>累计方</span></div>
            </div>
            <div class="c-actions">
              <el-button size="small" type="primary" plain @click="openDetail(c)">详情</el-button>
              <el-button v-perm="'carrier:edit'" size="small" @click="openForm(c)">编辑</el-button>
              <el-button v-perm="'carrier:edit'" size="small" type="danger" text @click="remove(c)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="ght-card table-wrap">
        <el-table :data="list">
          <el-table-column label="承运商" min-width="200">
            <template #default="{ row }">
              <div class="flex-center gap-8"><span class="dot" :style="{ background: row.brandColor || '#1f7ae0' }" /><b>{{ row.carrierName }}</b></div>
            </template>
          </el-table-column>
          <el-table-column prop="contactName" label="联系人" width="100" />
          <el-table-column prop="contactPhone" label="联系电话/账号" width="130" />
          <el-table-column prop="vehicleCount" label="车辆数" width="80" />
          <el-table-column label="运输中" width="90"><template #default="{ row }"><span :class="{ t: row.inTransitVehicleCount }">{{ row.inTransitVehicleCount }}</span></template></el-table-column>
          <el-table-column label="运输趟数" width="90"><template #default="{ row }">{{ row.stat.tripCount }}</template></el-table-column>
          <el-table-column label="累计吨/方/件" min-width="170"><template #default="{ row }">{{ fmtNum(row.stat.totalTonnage) }} / {{ fmtNum(row.stat.totalVolume) }} / {{ row.stat.totalPieces }}</template></el-table-column>
          <el-table-column label="状态" width="90"><template #default="{ row }"><StatusTag :dict="ENABLE_STATUS" :value="row.status" /></template></el-table-column>
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button v-perm="'carrier:edit'" link @click="openForm(row)">编辑</el-button>
              <el-button v-perm="'carrier:edit'" link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <EmptyState v-if="!list.length && !loading" text="暂无承运商" />
      <el-pagination v-if="total > query.pageSize" class="pager" background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="load" />
    </div>

    <CarrierFormDialog v-model="formVisible" :data="formData" @saved="load" />
    <el-drawer v-model="detailVisible" size="960px" :title="detailTitle" destroy-on-close>
      <CarrierDetailPanel :carrier-id="detailId" @changed="load" />
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import ViewToggle from '@/components/common/ViewToggle.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import TruckSvg from '@/components/svg/TruckSvg.vue'
import CarrierFormDialog from './components/CarrierFormDialog.vue'
import CarrierDetailPanel from './components/CarrierDetailPanel.vue'
import { ENABLE_STATUS } from '@/utils/dict'
import { fmtNum } from '@/utils/format'
import { carrierPage, carrierDelete } from '@/api/carrier'

const view = ref('card')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const transitFilter = ref(false)
const query = reactive({ pageNum: 1, pageSize: 12, keyword: '' })
const formVisible = ref(false)
const formData = ref(null)
const detailVisible = ref(false)
const detailId = ref(null)
const detailTitle = ref('')
const truckTypes = ['SEMI_TRAILER', 'DUMP', 'CONTAINER', 'HEAVY_CARGO']

async function load() {
  loading.value = true
  try {
    const res = await carrierPage({ ...query, onlyInTransit: transitFilter.value })
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
function truckStyle(i, c) {
  const moving = i <= c.inTransitVehicleCount
  return moving ? { animationDelay: `${(i - 1) * -2.2}s`, bottom: `${i % 2 ? 14 : 30}px` } : { left: `${16 + (i - 1 - c.inTransitVehicleCount) * 70}px`, bottom: '22px' }
}
function openForm(c) {
  formData.value = c ? { ...c } : null
  formVisible.value = true
}
function openDetail(c) {
  detailId.value = c.id
  detailTitle.value = c.carrierName
  detailVisible.value = true
}
async function remove(c) {
  await ElMessageBox.confirm(`确定删除承运商【${c.carrierName}】及其车辆吗？`, '删除确认', { type: 'warning' })
  await carrierDelete(c.id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

<style scoped lang="scss">
.c-card { background: #fff; border: 1px solid var(--ght-line); border-radius: 18px; overflow: hidden; }
.c-scene { position: relative; height: 118px; overflow: hidden; cursor: pointer; background: linear-gradient(160deg, color-mix(in srgb, var(--brand) 18%, #fff), color-mix(in srgb, var(--brand) 6%, #fff)); }
.sky { position: absolute; right: -30px; top: -40px; width: 140px; height: 140px; border-radius: 50%; background: radial-gradient(circle, color-mix(in srgb, var(--brand) 30%, transparent), transparent 70%); }
.road { position: absolute; left: 0; right: 0; bottom: 10px; height: 40px; background: color-mix(in srgb, var(--brand) 12%, #e8edf4);
  i { position: absolute; left: 0; right: 0; top: 19px; height: 2px; background: repeating-linear-gradient(90deg, #fff 0 14px, transparent 14px 26px); animation: lane 0.7s linear infinite; } }
@keyframes lane { to { background-position: -26px 0; } }
.truck { position: absolute; &.go { left: -90px; animation: drive 9s linear infinite; } }
@keyframes drive { to { transform: translateX(480px); } }
.no-truck { position: absolute; inset: 0; display: grid; place-items: center; color: var(--ght-ink-3); font-size: 13px; }
.live { position: absolute; right: 12px; top: 10px; font-size: 12px; color: #fff; padding: 3px 10px; border-radius: 999px; background: var(--brand); box-shadow: 0 4px 10px -4px var(--brand); }
.c-body { padding: 14px 16px 16px; }
.c-name { font-weight: 700; font-size: 16px; cursor: pointer; &:hover { color: var(--ght-blue); } }
.c-contact { display: flex; align-items: center; gap: 4px; margin-top: 6px; }
.small { font-size: 12px; }
.c-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 6px; margin-top: 12px;
  div { background: #f6f8fc; border-radius: 10px; padding: 8px; display: flex; flex-direction: column; align-items: center; }
  b { font-size: 17px; } span { font-size: 11px; color: var(--ght-ink-3); } }
.c-actions { display: flex; gap: 6px; margin-top: 12px; .el-button { margin: 0; } }
.table-wrap { padding: 6px; }
.dot { width: 10px; height: 10px; border-radius: 50%; }
.t { color: var(--ght-transit); font-weight: 700; }
.pager { margin-top: 18px; justify-content: flex-end; }
</style>
