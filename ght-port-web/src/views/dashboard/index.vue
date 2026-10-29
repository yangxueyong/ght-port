<template>
  <div class="ght-page dash">
    <div class="welcome">
      <div>
        <div class="hello">{{ greeting }}，{{ store.info?.nickname }}</div>
        <div class="muted">{{ store.info?.tenantName }} · {{ today }} · 让多式联运通达顺远</div>
      </div>
      <div class="quick">
        <el-button v-if="store.hasPerm('berth:board:list')" type="primary" icon="Ship" @click="$router.push('/berth')">泊位作业</el-button>
        <el-button v-if="store.hasPerm('port:map:list')" icon="MapLocation" @click="$router.push('/port')">3D 港口</el-button>
        <el-button v-if="store.hasPerm('carrier:list')" icon="Van" @click="$router.push('/carrier')">承运商</el-button>
      </div>
    </div>

    <div class="tiles">
      <StatTile label="正在作业泊位" :value="o.berthWorking" :hint="`共 ${berthTotal} 个泊位`" icon="Ship" color="#f59f00" />
      <StatTile label="运输中车辆" :value="o.vehicleInTransit" :hint="`车辆总数 ${o.vehicleCount ?? 0}`" icon="Van" color="#1f7ae0" />
      <StatTile label="今日出港" :value="fmtNum(o.todayTonnage, 1)" unit="吨" :hint="`今日出发 ${o.todayTrips ?? 0} 车次`" icon="TrendCharts" color="#12b886" />
      <StatTile label="待核销出门条" :value="o.unverifiedPasses" :hint="`承运商 ${o.carrierCount ?? 0} 家 · 船运公司 ${o.companyCount ?? 0} 家`" icon="Tickets" color="#7c5cff" />
    </div>

    <div class="grid">
      <div class="ght-card map-card">
        <div class="flex-between head">
          <div class="section-title">港区实时态势 <span class="muted small">{{ detail?.port?.portName }}</span></div>
          <div class="status-bar">
            <span v-for="s in statusList" :key="s.key" :style="{ '--c': s.color }"><i />{{ s.label }} <b class="num">{{ s.count }}</b></span>
          </div>
        </div>
        <div class="map">
          <PortCanvas3D v-if="detail && detail.elements.length" :elements="elements" :berth-map="berthMap" :width="detail.port.canvasWidth" :height="detail.port.canvasHeight"
            :theme="detail.port.theme" mode="berth" initial-view="top" view-switch :view-key="`dashboard:${detail.port.id}`" @berth-click="openBerth" />
          <EmptyState v-else text="暂无港口 3D 场景" />
        </div>
      </div>

      <div class="ght-card feed">
        <div class="section-title head">最新运输动态</div>
        <EmptyState v-if="!o.recentTrips || !o.recentTrips.length" text="暂无运输记录" />
        <div v-for="t in o.recentTrips" :key="t.id" class="feed-item">
          <div class="feed-truck">
            <TruckSvg :type="t.vehicleType" :length="9" :color="t.bodyColor || '#1f7ae0'" :moving="t.status === 'IN_TRANSIT'" :width="74" />
          </div>
          <div class="feed-body">
            <div class="flex-between"><b>{{ t.plateNo }}</b><StatusTag :dict="TRIP_STATUS" :value="t.status" :pulse="t.status === 'IN_TRANSIT'" /></div>
            <div class="small">{{ t.cargoName }} · <span class="num">{{ fmtNum(t.loadTonnage, 2) }}吨</span></div>
            <div class="muted small">{{ t.carrierName }} · {{ t.berthName }} · {{ fromNow(t.departTime) }}</div>
          </div>
        </div>
      </div>
    </div>
    <BerthDetailDrawer v-model="berthDrawer" :berth-id="berthId" readonly />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import StatTile from '@/components/common/StatTile.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import PortCanvas3D from '@/components/port3d/PortCanvas3D.vue'
import TruckSvg from '@/components/svg/TruckSvg.vue'
import BerthDetailDrawer from '@/views/berth/components/BerthDetailDrawer.vue'
import { toModel } from '@/components/port/elementRegistry'
import { dashboardOverview, portList, portDetail } from '@/api/port'
import { TRIP_STATUS } from '@/utils/dict'
import { fmtNum, fromNow } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const o = ref({})
const detail = ref(null)
const today = dayjs().format('YYYY年MM月DD日 dddd')
const greeting = computed(() => {
  const h = new Date().getHours()
  return h < 6 ? '夜深了' : h < 11 ? '早上好' : h < 14 ? '中午好' : h < 18 ? '下午好' : '晚上好'
})
const berthTotal = computed(() => (o.value.berthIdle || 0) + (o.value.berthWorking || 0) + (o.value.berthMaintenance || 0))
const statusList = computed(() => [
  { key: 'IDLE', label: '未作业', color: '#12b886', count: o.value.berthIdle || 0 },
  { key: 'WORKING', label: '作业中', color: '#f59f00', count: o.value.berthWorking || 0 },
  { key: 'MAINTENANCE', label: '维修中', color: '#e5484d', count: o.value.berthMaintenance || 0 }
])
const elements = computed(() => (detail.value?.elements || []).map(toModel))
const berthDrawer = ref(false)
const berthId = ref(null)
/** 工作台点击泊位：只读查看泊位详情，不跳转 */
function openBerth(b) {
  berthId.value = b.id
  berthDrawer.value = true
}
const berthMap = computed(() => Object.fromEntries((detail.value?.berths || []).map((b) => [b.id, b])))

onMounted(async () => {
  o.value = await dashboardOverview()
  if (store.hasPerm(['port:map:list', 'berth:board:list'])) {
    const ports = await portList()
    if (ports.length) detail.value = await portDetail(ports[0].id)
  }
})
</script>

<style scoped lang="scss">
.welcome { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; flex-wrap: wrap; gap: 12px; }
.hello { font-size: 24px; font-weight: 800; margin-bottom: 6px; }
.quick { display: flex; gap: 8px; }
.tiles { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.grid { display: grid; grid-template-columns: minmax(0, 1fr) 340px; gap: 16px; margin-top: 16px; }
.head { padding: 14px 16px 10px; }
.small { font-size: 12px; }
.map-card { overflow: hidden; }
.map { height: 470px; margin: 0 16px 16px; border-radius: 12px; overflow: hidden; box-shadow: inset 0 0 0 1px rgba(47, 91, 211, 0.18); }
.status-bar { display: flex; gap: 14px; font-size: 12px; color: var(--ght-ink-2); span { display: flex; align-items: center; gap: 5px; } i { width: 8px; height: 8px; border-radius: 50%; background: var(--c); } b { color: var(--c); font-size: 15px; } }
.feed { padding-bottom: 8px; max-height: 556px; overflow: auto; }
.feed-item { display: flex; gap: 10px; padding: 10px 16px; border-top: 1px solid #f0f3f8; }
.feed-truck { width: 74px; flex-shrink: 0; display: flex; align-items: center; }
.feed-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
@media (max-width: 1280px) { .grid { grid-template-columns: 1fr; } .tiles { grid-template-columns: repeat(2, 1fr); } }
</style>
