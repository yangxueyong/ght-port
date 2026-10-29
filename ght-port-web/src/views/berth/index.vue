<template>
  <div class="ght-page">
    <PageHeader title="泊位管理" icon="Ship" subtitle="实时查看各泊位作业状态，点击泊位查看货物、承运商、车辆与船只，进行到货、装货登记及分派承运/船只">
      <el-segmented v-if="ports.length > 1" v-model="portId" :options="portOptions" @change="load" />
      <el-button icon="Refresh" @click="load">刷新</el-button>
      <ViewToggle v-model="view" storage-key="berth" />
    </PageHeader>

    <div class="summary">
      <div v-for="s in summary" :key="s.key" class="sum-item" :class="[s.key, { on: statusFilter === s.key }]" @click="statusFilter = statusFilter === s.key ? '' : s.key">
        <div class="sum-icon"><el-icon><component :is="s.icon" /></el-icon></div>
        <div>
          <div class="sum-val num">{{ s.count }}</div>
          <div class="sum-label">{{ s.label }}</div>
        </div>
        <div class="sum-wave" />
      </div>
    </div>

    <div class="map-card ght-card" v-loading="loading">
      <div class="map-head">
        <div class="section-title">{{ detail?.port?.portName || '港口' }} · 3D 实时态势</div>
        <div class="legend">
          <span><i class="lg IDLE" />未作业</span>
          <span><i class="lg WORKING" />正在作业</span>
          <span><i class="lg MAINTENANCE" />维修中</span>
          <span class="muted">点击泊位查看详情 · 左键旋转 · 滚轮缩放</span>
        </div>
      </div>
      <div class="map-body">
        <PortCanvas3D v-if="detail && detail.elements.length" :elements="mapElements" :berth-map="berthMap" :width="detail.port.canvasWidth" :height="detail.port.canvasHeight"
          :theme="detail.port.theme" mode="berth" initial-view="top" view-switch :view-key="`berth:${detail.port.id}`" @berth-click="openBerth" />
        <EmptyState v-else-if="!loading" text="该港口尚未搭建 3D 场景，请先到“港口管理”中搭建" />
      </div>
    </div>

    <div class="flex-between mt-16 list-head">
      <div class="section-title">泊位列表</div>
      <el-input v-model="keyword" placeholder="搜索泊位编号/名称" prefix-icon="Search" clearable style="width: 240px" />
    </div>

    <div v-if="view === 'card'" class="card-grid mt-12">
      <div v-for="b in filteredBerths" :key="b.id" class="berth-card hover-lift" :class="b.status" @click="openBerth(b)">
        <div class="bc-visual">
          <img v-if="thumbs[thumbKey(b)]" :src="thumbs[thumbKey(b)]" :alt="b.berthName" class="bc-img" />
          <div class="bc-chip num">{{ b.berthCode }}</div>
        </div>
        <div class="bc-body">
          <div class="flex-between">
            <div class="bc-name">{{ b.berthName }}</div>
            <StatusTag :dict="BERTH_STATUS" :value="b.status" :pulse="b.status !== 'IDLE'" />
          </div>
          <div class="bc-meta muted">
            <span><el-icon><Clock /></el-icon>{{ b.workStartTime ? b.workStartTime.slice(0, 5) + '-' + b.workEndTime.slice(0, 5) : '未设置作业时间' }}</span>
            <span>{{ b.maxTonnage ? b.maxTonnage + '吨级' : '' }}</span>
          </div>
          <div class="bc-stats">
            <div><b class="num">{{ b.activeCargoCount }}</b><span>作业货物</span></div>
            <div><b class="num w">{{ b.waitingCount }}</b><span>待装载</span></div>
            <div><b class="num t">{{ b.inTransitCount }}</b><span>运输中</span></div>
          </div>
        </div>
      </div>
      <EmptyState v-if="!filteredBerths.length && !loading" text="没有符合条件的泊位" />
    </div>

    <div v-else class="ght-card mt-12 table-wrap">
      <el-table :data="filteredBerths" @row-click="openBerth" row-class-name="clickable-row">
        <el-table-column prop="berthCode" label="编号" width="90" />
        <el-table-column prop="berthName" label="泊位名称" min-width="140" />
        <el-table-column label="状态" width="120"><template #default="{ row }"><StatusTag :dict="BERTH_STATUS" :value="row.status" /></template></el-table-column>
        <el-table-column label="作业时间" width="140"><template #default="{ row }">{{ row.workStartTime ? row.workStartTime.slice(0, 5) + ' - ' + row.workEndTime.slice(0, 5) : '-' }}</template></el-table-column>
        <el-table-column prop="berthLength" label="长度(米)" width="100" />
        <el-table-column prop="waterDepth" label="水深(米)" width="100" />
        <el-table-column prop="maxTonnage" label="吨级" width="100" />
        <el-table-column prop="activeCargoCount" label="作业货物" width="100" />
        <el-table-column prop="waitingCount" label="待装载" width="90" />
        <el-table-column prop="inTransitCount" label="运输中" width="90" />
        <el-table-column label="操作" width="100" fixed="right"><template #default="{ row }"><el-button link type="primary" @click.stop="openBerth(row)">详情</el-button></template></el-table-column>
      </el-table>
    </div>

    <BerthDetailDrawer v-model="drawer" :berth-id="currentBerthId" @changed="load" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import ViewToggle from '@/components/common/ViewToggle.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import PortCanvas3D from '@/components/port3d/PortCanvas3D.vue'
import { renderThumb } from '@/components/port3d/engine/thumbs'
import BerthDetailDrawer from './components/BerthDetailDrawer.vue'
import { toModel } from '@/components/port/elementRegistry'
import { BERTH_STATUS } from '@/utils/dict'
import { portList, portDetail } from '@/api/port'
const view = ref('card')
const ports = ref([])
const portId = ref(null)
const detail = ref(null)
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref('')
const drawer = ref(false)
const currentBerthId = ref(null)

const portOptions = computed(() => ports.value.map((p) => ({ label: p.portName, value: p.id })))
const berths = computed(() => detail.value?.berths || [])
const berthMap = computed(() => Object.fromEntries(berths.value.map((b) => [b.id, b])))
const mapElements = computed(() => (detail.value?.elements || []).map(toModel))
const filteredBerths = computed(() =>
  berths.value.filter(
    (b) => (!statusFilter.value || b.status === statusFilter.value) && (!keyword.value || (b.berthName + b.berthCode).includes(keyword.value))
  )
)
const summary = computed(() => [
  { key: 'IDLE', label: '未作业', icon: 'CircleCheck', count: berths.value.filter((b) => b.status === 'IDLE').length },
  { key: 'WORKING', label: '正在作业', icon: 'Loading', count: berths.value.filter((b) => b.status === 'WORKING').length },
  { key: 'MAINTENANCE', label: '维修中', icon: 'Tools', count: berths.value.filter((b) => b.status === 'MAINTENANCE').length },
  { key: 'TRANSIT', label: '运输中车辆', icon: 'Van', count: berths.value.reduce((s, b) => s + (b.inTransitCount || 0), 0) }
])

async function load() {
  loading.value = true
  try {
    if (!ports.value.length) {
      ports.value = await portList()
      portId.value = ports.value[0]?.id || null
    }
    detail.value = portId.value ? await portDetail(portId.value) : null
  } finally {
    loading.value = false
  }
}
/* ---------- 泊位卡片 3D 缩略图 ---------- */
const thumbs = reactive({})
const thumbKey = (b) => `${b.berthCode}|${b.berthName}|${b.status}`
watch(
  filteredBerths,
  (list) => {
    const todo = list.filter((b) => !thumbs[thumbKey(b)])
    const next = (i) => {
      if (i >= todo.length) return
      const b = todo[i]
      try {
        thumbs[thumbKey(b)] = renderThumb(
          { uid: 'card-' + b.id, elementType: 'BERTH', width: 190, height: 86, cfg: {}, berthCode: b.berthCode, berthName: b.berthName },
          { berth: b, water: true, w: 420, h: 220, zoom: 0.74, key: 'card|' + thumbKey(b) }
        )
      } catch (e) {
        console.warn('泊位缩略图渲染失败', e)
      }
      setTimeout(() => next(i + 1), 16)
    }
    next(0)
  },
  { immediate: true }
)
function openBerth(b) {
  currentBerthId.value = b.id
  drawer.value = true
}
let timer = null
onMounted(() => {
  load()
  timer = setInterval(() => !drawer.value && load(), 30000)
})
onBeforeUnmount(() => clearInterval(timer))
</script>

<style scoped lang="scss">
.summary { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 16px; }
.sum-item {
  position: relative; overflow: hidden; display: flex; align-items: center; gap: 14px; padding: 16px 18px;
  background: #fff; border: 1px solid var(--ght-line); border-radius: 16px; cursor: pointer; transition: all 0.2s;
  --c: #12b886;
  &.WORKING { --c: #f59f00; }
  &.MAINTENANCE { --c: #e5484d; }
  &.TRANSIT { --c: #1f7ae0; cursor: default; }
  &.on { border-color: var(--c); box-shadow: 0 10px 24px -14px var(--c); }
  &:hover { transform: translateY(-2px); }
}
.sum-icon { width: 44px; height: 44px; border-radius: 13px; display: grid; place-items: center; font-size: 22px; color: var(--c); background: color-mix(in srgb, var(--c) 12%, #fff); }
.WORKING .sum-icon .el-icon { animation: spin 2.4s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.sum-val { font-size: 26px; font-weight: 800; line-height: 1.1; }
.sum-label { font-size: 13px; color: var(--ght-ink-3); }
.sum-wave { position: absolute; right: -20px; bottom: -30px; width: 120px; height: 80px; border-radius: 50%; background: color-mix(in srgb, var(--c) 10%, transparent); }
.map-card { padding: 14px 16px 16px; }
.map-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; flex-wrap: wrap; gap: 8px; }
.legend { display: flex; gap: 14px; font-size: 12px; color: var(--ght-ink-2); align-items: center; }
.lg { display: inline-block; width: 10px; height: 10px; border-radius: 3px; margin-right: 5px; vertical-align: -1px;
  &.IDLE { background: var(--ght-idle); } &.WORKING { background: var(--ght-working); } &.MAINTENANCE { background: repeating-linear-gradient(45deg, #f8c22e 0 3px, #2b2f36 3px 6px); } }
.map-body { height: 560px; border-radius: 12px; overflow: hidden; box-shadow: inset 0 0 0 1px rgba(47, 91, 211, 0.18); }
.berth-card { background: #fff; border-radius: 16px; border: 1px solid var(--ght-line); overflow: hidden; cursor: pointer;
  &.WORKING { border-top: 3px solid var(--ght-working); } &.IDLE { border-top: 3px solid var(--ght-idle); } &.MAINTENANCE { border-top: 3px solid var(--ght-maint); } }
.bc-visual { position: relative; height: 150px; background: radial-gradient(120% 100% at 50% 20%, #f7faff, #e3ebf7); overflow: hidden; }
.bc-img { width: 100%; height: 100%; object-fit: cover; display: block; transition: transform 0.4s cubic-bezier(0.2, 0.8, 0.2, 1); }
.berth-card:hover .bc-img { transform: scale(1.06); }
.bc-chip { position: absolute; left: 10px; top: 10px; padding: 2px 10px; border-radius: 6px; font-size: 13px; font-weight: 800; color: #fff; background: rgba(30, 58, 138, 0.85); letter-spacing: 1px; }
.bc-body { padding: 12px 14px 14px; }
.bc-name { font-weight: 700; font-size: 15px; }
.bc-meta { display: flex; justify-content: space-between; font-size: 12px; margin-top: 6px; span { display: flex; align-items: center; gap: 4px; } }
.bc-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-top: 12px;
  div { background: #f6f8fc; border-radius: 10px; padding: 8px 10px; display: flex; flex-direction: column; }
  b { font-size: 20px; &.w { color: var(--ght-working); } &.t { color: var(--ght-transit); } }
  span { font-size: 12px; color: var(--ght-ink-3); } }
.table-wrap { padding: 6px; }
:deep(.clickable-row) { cursor: pointer; }
@media (max-width: 1100px) { .summary { grid-template-columns: repeat(2, 1fr); } }
</style>
