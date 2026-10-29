<template>
  <div>
    <div class="filters">
      <el-segmented v-model="query.status" :options="statusOptions" @change="search" />
      <el-input v-model="query.keyword" placeholder="搜索车牌/出门条号/司机" prefix-icon="Search" clearable style="width: 240px" @change="search" />
      <span class="flex-1" />
      <ViewToggle v-model="view" :storage-key="storageKey" />
    </div>
    <div v-loading="loading">
      <div v-if="view === 'card'" class="pass-grid">
        <div v-for="p in list" :key="p.id" class="pass-card hover-lift" :class="p.status" @click="open(p)">
          <div class="pc-left">
            <TruckSvg :type="p.vehicleType" :length="9" color="#1f7ae0" :moving="p.status === 'UNUSED'" :width="110" />
            <div class="pc-plate">{{ p.plateNo }}</div>
          </div>
          <div class="pc-right">
            <div class="flex-between">
              <span class="num pc-no">{{ p.passNo }}</span>
              <StatusTag :dict="PASS_STATUS" :value="p.status" :pulse="p.status === 'UNUSED'" />
            </div>
            <div class="pc-cargo">{{ p.cargoName }} <span class="muted">{{ p.cargoTypeName }}</span></div>
            <div class="pc-load num">{{ fmtNum(p.loadTonnage, 3) }}吨 · {{ fmtNum(p.loadVolume, 3) }}方 · {{ p.loadPieces }}件</div>
            <div class="muted small">{{ p.carrierName }} · {{ p.driverName }}</div>
            <div class="muted small">开具 {{ fmtTime(p.issueTime, 'MM-DD HH:mm') }}<template v-if="p.verifyTime"> · 核销 {{ fmtTime(p.verifyTime, 'MM-DD HH:mm') }}</template></div>
          </div>
          <div class="notch top" /><div class="notch bottom" />
        </div>
      </div>
      <div v-else class="ght-card table-wrap">
        <el-table :data="list" @row-click="open">
          <el-table-column prop="passNo" label="出门条编号" width="190" />
          <el-table-column prop="plateNo" label="车牌" width="110" />
          <el-table-column prop="carrierName" label="承运商" min-width="160" show-overflow-tooltip />
          <el-table-column prop="driverName" label="司机" width="90" />
          <el-table-column prop="cargoName" label="货物" min-width="160" show-overflow-tooltip />
          <el-table-column label="装载量" width="190"><template #default="{ row }">{{ fmtNum(row.loadTonnage, 3) }}吨/{{ fmtNum(row.loadVolume, 3) }}方/{{ row.loadPieces }}件</template></el-table-column>
          <el-table-column label="开具时间" width="140"><template #default="{ row }">{{ fmtTime(row.issueTime, 'MM-DD HH:mm') }}</template></el-table-column>
          <el-table-column label="状态" width="100"><template #default="{ row }"><StatusTag :dict="PASS_STATUS" :value="row.status" /></template></el-table-column>
          <el-table-column label="核销" width="160"><template #default="{ row }">{{ row.verifyTime ? fmtTime(row.verifyTime, 'MM-DD HH:mm') + ' ' + (row.verifyBy || '') : '-' }}</template></el-table-column>
        </el-table>
      </div>
      <EmptyState v-if="!list.length && !loading" text="暂无出门条" />
      <el-pagination v-if="total > query.pageSize" class="pager" background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="load" />
    </div>
    <GatePassDialog v-model="visible" :pass="current" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import ViewToggle from '@/components/common/ViewToggle.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import TruckSvg from '@/components/svg/TruckSvg.vue'
import GatePassDialog from '@/views/berth/components/GatePassDialog.vue'
import { PASS_STATUS } from '@/utils/dict'
import { fmtNum, fmtTime } from '@/utils/format'

const props = defineProps({ api: { type: Function, required: true }, storageKey: String })
const view = ref('card')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const visible = ref(false)
const current = ref(null)
const query = reactive({ pageNum: 1, pageSize: 12, keyword: '', status: '' })
const statusOptions = [
  { label: '全部', value: '' },
  { label: '未核销', value: 'UNUSED' },
  { label: '已核销', value: 'VERIFIED' }
]
async function load() {
  loading.value = true
  try {
    const res = await props.api(query)
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
function open(p) {
  current.value = p
  visible.value = true
}
onMounted(load)
</script>

<style scoped lang="scss">
.filters { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.flex-1 { flex: 1; }
.pass-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(380px, 1fr)); gap: 16px; }
.pass-card {
  position: relative; display: flex; background: #fff; border-radius: 16px; border: 1px solid var(--ght-line); cursor: pointer; overflow: hidden;
  &.UNUSED .pc-left { background: linear-gradient(160deg, #fff4de, #fffaf0); }
  &.VERIFIED .pc-left { background: linear-gradient(160deg, #e6f8f1, #f5fcf9); }
}
.pc-left { width: 140px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; border-right: 2px dashed #e1e7f0; padding: 12px; }
.pc-plate { font-weight: 800; padding: 2px 8px; border-radius: 5px; background: #ffd43b; border: 2px solid #1d2433; font-size: 13px; }
.pc-right { flex: 1; padding: 12px 14px; display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.pc-no { font-size: 12px; color: var(--ght-ink-3); }
.pc-cargo { font-weight: 700; font-size: 15px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; .muted { font-weight: 400; font-size: 12px; } }
.pc-load { color: var(--ght-blue); font-weight: 600; }
.small { font-size: 12px; }
.notch { position: absolute; left: 133px; width: 14px; height: 14px; border-radius: 50%; background: var(--ght-bg); border: 1px solid var(--ght-line); &.top { top: -8px; } &.bottom { bottom: -8px; } }
.table-wrap { padding: 6px; }
.pager { margin-top: 18px; justify-content: flex-end; }
</style>
