<template>
  <div ref="rootRef" class="cockpit" :class="{ 'is-full': fullscreen }">
    <!-- 3D 港口作为整页背景 -->
    <div class="scene">
      <PortCanvas3D v-if="d && elements.length" ref="canvasRef" :elements="elements" :berth-map="berthMap" :width="d.port.port.canvasWidth" :height="d.port.port.canvasHeight"
        :theme="d.port.port.theme" mode="berth" initial-view="iso" :insets="insets" :focus-blur="follow" :auto-rotate="autoRotate" @berth-click="focusBerth" />
      <div v-else-if="d" class="scene-empty">该港口尚未搭建 3D 场景</div>
      <div class="shade" />
    </div>

    <!-- 浮层：只有面板拦截鼠标，空白处可直接操作 3D 场景 -->
    <div class="overlay">
      <header ref="headerRef" class="top">
        <div class="top-left">
          <span class="live"><i class="status-dot pulse" />实时数据</span>
          <span class="clock num">{{ now }}</span>
        </div>
        <div class="title">
          <div class="t-main">{{ portName }} · 智慧港口驾驶舱</div>
          <div class="t-sub">PORT DIGITAL TWIN COCKPIT</div>
          <div class="t-line"><i /></div>
        </div>
        <div class="top-right">
          <el-select v-if="ports.length > 1" v-model="portId" size="small" class="port-sel" @change="load">
            <el-option v-for="p in ports" :key="p.id" :label="p.portName" :value="p.id" />
          </el-select>
          <span class="seg views">
            <span @click="setView('iso')">斜视</span>
            <span @click="setView('top')">俯视</span>
          </span>
          <button class="tbtn" :class="{ on: follow }" :title="follow ? '轮播时镜头定位到当前泊位' : '保持全景视角'" @click="follow = !follow"><el-icon><Aim /></el-icon><span class="txt">{{ follow ? '镜头跟随' : '全景视角' }}</span></button>
          <button class="tbtn" :class="{ on: autoRotate }" title="自动旋转" @click="autoRotate = !autoRotate"><el-icon><Refresh /></el-icon><span class="txt">{{ autoRotate ? '旋转中' : '自动旋转' }}</span></button>
          <button class="tbtn icon" title="刷新" @click="load"><el-icon><RefreshRight /></el-icon></button>
          <button class="tbtn primary" @click="toggleFullscreen"><el-icon><component :is="fullscreen ? 'Close' : 'FullScreen'" /></el-icon>{{ fullscreen ? '退出全屏' : '全屏展示' }}</button>
        </div>
      </header>

      <div v-if="d" class="body">
        <!-- 左栏 -->
        <section ref="leftRef" class="col side">
          <div class="glass">
            <div class="p-title">港口累计<small>PORT TOTAL</small></div>
            <div class="kpi-grid">
              <div class="kpi u">
                <span class="k-label">累计卸货</span>
                <b><CountUp :value="d.summary.unloadTonnage" :digits="1" /><small>吨</small></b>
                <em>{{ compact(d.summary.unloadVolume) }} 方 · {{ d.summary.unloadCount }} 批次</em>
              </div>
              <div class="kpi l">
                <span class="k-label">累计装货</span>
                <b><CountUp :value="d.summary.loadTonnage" :digits="1" /><small>吨</small></b>
                <em>{{ compact(d.summary.loadVolume) }} 方 · {{ d.summary.loadCount }} 批次</em>
              </div>
              <div class="kpi">
                <span class="k-label">累计作业次数</span>
                <b><CountUp :value="d.summary.workCount" /><small>次</small></b>
                <em>到货 {{ d.summary.unloadCount }} · 装货 {{ d.summary.loadCount }}</em>
              </div>
              <div class="kpi t">
                <span class="k-label">运输车次</span>
                <b><CountUp :value="d.summary.tripCount" /><small>车</small></b>
                <em>运出 {{ compact(d.summary.tripTonnage) }} 吨</em>
              </div>
            </div>
          </div>
          <div class="glass">
            <div class="p-title">泊位状态<small>BERTH STATUS</small></div>
            <StatusDonut :berths="berths" />
          </div>
          <div class="glass grow">
            <div class="p-title">货物类型统计<small>BY CARGO TYPE</small></div>
            <CargoTypeBars :data="d.cargoTypes" :limit="7" />
          </div>
        </section>

        <!-- 中间：留空展示 3D 场景，顶部指标条 + 底部趋势 -->
        <section class="col center">
          <div ref="chipsRef" class="chips">
            <div class="chip"><span>运输中车辆</span><b class="num t">{{ d.summary.inTransitCount }}</b></div>
            <div class="chip"><span>作业船只</span><b class="num w">{{ d.summary.workingShipCount }}</b></div>
            <div class="chip"><span>承运商 / 车辆</span><b class="num">{{ d.summary.carrierCount }}<small> / {{ d.summary.vehicleCount }}</small></b></div>
            <div class="chip"><span>船运公司 / 船只</span><b class="num">{{ d.summary.companyCount }}<small> / {{ d.summary.shipCount }}</small></b></div>
          </div>
          <div class="spacer" />
          <div class="scene-tip">点击泊位定位 · 左键旋转 · 右键平移 · 滚轮缩放</div>
          <div ref="trendRef" class="glass trend-panel">
            <div class="p-title">近 7 日装卸趋势<small>7-DAY TREND</small></div>
            <TrendChart :data="d.trend" />
          </div>
        </section>

        <!-- 右栏 -->
        <section ref="rightRef" class="col side">
          <div class="glass carousel-panel">
            <div class="p-title">泊位轮播<small>BERTH CAROUSEL</small></div>
            <BerthCarousel v-model:index="berthIndex" :berths="berths" :stats="statMap" />
          </div>
          <div class="glass rank-panel">
            <div class="p-title">
              {{ rankMode === 'carrier' ? '承运商统计' : '船运公司统计' }}<small>{{ rankMode === 'carrier' ? 'CARRIERS' : 'SHIPPERS' }}</small>
              <span class="seg">
                <span :class="{ on: rankMode === 'carrier' }" @click="rankMode = 'carrier'">承运商</span>
                <span :class="{ on: rankMode === 'shipper' }" @click="rankMode = 'shipper'">船运公司</span>
              </span>
            </div>
            <transition name="fade" mode="out-in">
              <RankList :key="rankMode" :data="rankMode === 'carrier' ? d.carriers : d.companies" :mode="rankMode" :limit="4" />
            </transition>
          </div>
        </section>
      </div>
      <div v-else class="loading-box glass">{{ error || '正在加载驾驶舱数据…' }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import PortCanvas3D from '@/components/port3d/PortCanvas3D.vue'
import { toModel } from '@/components/port/elementRegistry'
import CountUp from './components/CountUp.vue'
import StatusDonut from './components/StatusDonut.vue'
import CargoTypeBars from './components/CargoTypeBars.vue'
import TrendChart from './components/TrendChart.vue'
import BerthCarousel from './components/BerthCarousel.vue'
import RankList from './components/RankList.vue'
import { compact } from './components/palette'
import { cockpitOverview, portList } from '@/api/port'

const d = ref(null)
const error = ref('')
const ports = ref([])
const portId = ref(null)
const now = ref('')
const fullscreen = ref(false)
const autoRotate = ref(false)
const berthIndex = ref(0)
const rankMode = ref('carrier')
const canvasRef = ref()
const rootRef = ref()
const headerRef = ref()
const leftRef = ref()
const rightRef = ref()
const chipsRef = ref()
const trendRef = ref()
/** 浮层遮挡的边距：3D 画面以未遮挡区域为中心，泊位定位不会被面板挡住 */
const insets = ref(null)
/** 镜头跟随：泊位轮播时 3D 场景定位到同一泊位 */
const follow = ref(true)

const portName = computed(() => d.value?.port.port.portName || '港慧通')
const elements = computed(() => (d.value?.port.elements || []).map(toModel))
const berths = computed(() => [...(d.value?.port.berths || [])].sort((a, b) => String(a.berthCode).localeCompare(String(b.berthCode), 'zh', { numeric: true })))
const berthMap = computed(() => Object.fromEntries(berths.value.map((b) => [b.id, b])))
const statMap = computed(() => Object.fromEntries((d.value?.berthStats || []).map((s) => [s.berthId, s])))

async function load() {
  try {
    if (!ports.value.length) {
      ports.value = await portList()
      portId.value = ports.value[0]?.id || null
    }
    d.value = await cockpitOverview(portId.value)
    error.value = ''
    if (berthIndex.value >= berths.value.length) berthIndex.value = 0
  } catch (e) {
    error.value = e?.message || '驾驶舱数据加载失败'
  }
}
const curBerthId = computed(() => berths.value[berthIndex.value]?.id || null)
// 轮播/点击切换泊位：仅在“镜头跟随”开启时定位，关闭时保持用户当前的视角（不复位）
watch(
  [curBerthId, () => elements.value.length],
  async ([id]) => {
    if (!follow.value) return
    await nextTick()
    canvasRef.value?.focusBerth(id)
  },
  { flush: 'post' }
)
// 切换“镜头跟随”：开启时定位到当前泊位，关闭时回到全景（只在切换的这一刻执行一次）
let pendingView = null
watch(follow, async (on) => {
  await nextTick()
  if (on) canvasRef.value?.focusBerth(curBerthId.value)
  else if (pendingView) canvasRef.value?.fit(pendingView)
  else canvasRef.value?.focusBerth(null)
  pendingView = null
})

/** 测量浮层面板位置，换算为 3D 场景四周被遮挡的像素 */
function measure() {
  const root = rootRef.value?.getBoundingClientRect()
  const l = leftRef.value?.getBoundingClientRect()
  const r = rightRef.value?.getBoundingClientRect()
  const top = (chipsRef.value || headerRef.value)?.getBoundingClientRect()
  const t = trendRef.value?.getBoundingClientRect()
  if (!root || !l || !r) return
  const next = {
    left: Math.round(l.right - root.left),
    right: Math.round(root.right - r.left),
    top: Math.round((top ? top.bottom : root.top) - root.top),
    bottom: Math.round(t ? root.bottom - t.top : 0)
  }
  const cur = insets.value
  if (!cur || Object.keys(next).some((k) => Math.abs(next[k] - cur[k]) > 2)) insets.value = next
}
let ro = null
watch(
  () => !!d.value,
  async (ok) => {
    if (!ok) return
    await nextTick()
    measure()
    if (!ro && rootRef.value) {
      ro = new ResizeObserver(measure)
      ro.observe(rootRef.value)
    }
  }
)

/** 视角切换（斜视/俯视）：关闭镜头跟随并切到对应全景视角 */
function setView(v) {
  if (follow.value) {
    pendingView = v
    follow.value = false
  } else {
    canvasRef.value?.focusBerth(null)
    canvasRef.value?.fit(v)
  }
}

function focusBerth(b) {
  const i = berths.value.findIndex((x) => x.id === b.id)
  if (i >= 0) berthIndex.value = i
}

/* ---------- 一键全屏：页面铺满窗口，并请求浏览器全屏（documentElement，弹层仍可见） ---------- */
async function toggleFullscreen() {
  fullscreen.value = !fullscreen.value
  try {
    if (fullscreen.value && !document.fullscreenElement) await document.documentElement.requestFullscreen?.()
    else if (!fullscreen.value && document.fullscreenElement) await document.exitFullscreen?.()
  } catch (e) {
    // 浏览器不允许全屏时仅铺满窗口
  }
}
function onFsChange() {
  if (!document.fullscreenElement && fullscreen.value) fullscreen.value = false
}
function onKey(e) {
  if (e.key === 'Escape' && fullscreen.value && !document.fullscreenElement) fullscreen.value = false
}

let clock = null
let refresher = null
let rankTimer = null
onMounted(() => {
  load()
  const tick = () => (now.value = dayjs().format('YYYY-MM-DD HH:mm:ss'))
  tick()
  clock = setInterval(tick, 1000)
  refresher = setInterval(load, 30000)
  rankTimer = setInterval(() => (rankMode.value = rankMode.value === 'carrier' ? 'shipper' : 'carrier'), 15000)
  document.addEventListener('fullscreenchange', onFsChange)
  window.addEventListener('keydown', onKey)
})
onBeforeUnmount(() => {
  clearInterval(clock)
  clearInterval(refresher)
  clearInterval(rankTimer)
  ro?.disconnect()
  document.removeEventListener('fullscreenchange', onFsChange)
  window.removeEventListener('keydown', onKey)
  if (fullscreen.value && document.fullscreenElement) document.exitFullscreen?.()
})
</script>

<style scoped lang="scss">
.cockpit {
  --ink: #142033; --ink-2: #3b4a63; --ink-3: #6b7a92; --accent: #2f5bd3; --tech: #22c3e6;
  position: relative; overflow: clip; height: calc(100vh - 96px); min-height: 720px; border-radius: 18px; color: var(--ink);
  background: radial-gradient(120% 90% at 50% 30%, #f6f9fd 0%, #e6edf7 60%, #dbe4f1 100%);
  &.is-full { position: fixed; inset: 0; z-index: 1999; height: 100vh; min-height: 0; border-radius: 0; }
}

/* 背景 3D 场景 */
/* 层次：3D 场景(0) < 数据浮层(2)；定位虚化只作用于 3D 场景本身 */
.scene { position: absolute; inset: 0; z-index: 0; isolation: isolate; }
.scene :deep(.port3d) { background: transparent; }
.scene-empty { position: absolute; inset: 0; display: grid; place-items: center; color: var(--ink-3); }
/* 四周提亮的柔光遮罩，保证浮层文字清晰，中间保持通透 */
.shade { position: absolute; inset: 0; pointer-events: none;
  background:
    linear-gradient(180deg, rgba(240, 246, 255, 0.85) 0, rgba(240, 246, 255, 0) 120px),
    linear-gradient(90deg, rgba(240, 246, 255, 0.55) 0, rgba(240, 246, 255, 0) 26%, rgba(240, 246, 255, 0) 72%, rgba(240, 246, 255, 0.55) 100%),
    linear-gradient(0deg, rgba(240, 246, 255, 0.6) 0, rgba(240, 246, 255, 0) 200px); }

/* 浮层 */
.overlay { position: absolute; inset: 0; z-index: 2; display: flex; flex-direction: column; gap: 12px; padding: 12px 14px 14px; pointer-events: none;
  .top > *, .glass, .chip { pointer-events: auto; } }
.is-full .overlay { padding: 14px 18px 18px; }

/* 顶栏 */
.top { display: grid; grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr); align-items: center; gap: 12px; height: 58px; flex-shrink: 0; }
.top-left { display: flex; align-items: center; gap: 12px; justify-self: start; min-width: 0; white-space: nowrap; font-size: 13px; color: var(--ink-2); }
.live { display: flex; align-items: center; gap: 6px; padding: 4px 12px; border-radius: 999px; background: rgba(255, 255, 255, 0.7); border: 1px solid rgba(255, 255, 255, 0.9); backdrop-filter: blur(10px); color: #0d9488; font-weight: 600;
  .status-dot { background: #12b886; color: #12b886; } }
.clock { font-size: 15px; font-weight: 700; letter-spacing: 1px; color: var(--ink); padding: 4px 12px; border-radius: 999px; background: rgba(255, 255, 255, 0.55); backdrop-filter: blur(10px); }
.title { text-align: center; padding: 4px 28px 6px; border-radius: 0 0 22px 22px; }
.t-main { font-size: 26px; font-weight: 800; letter-spacing: 4px;
  background: linear-gradient(90deg, #1e3a8a, #2f5bd3 40%, #22a6e6 70%, #1e3a8a); background-size: 200% 100%; -webkit-background-clip: text; background-clip: text; color: transparent;
  animation: sheen 6s linear infinite; filter: drop-shadow(0 2px 8px rgba(255, 255, 255, 0.9)); }
@keyframes sheen { to { background-position: -200% 0; } }
.t-sub { font-size: 10px; letter-spacing: 6px; color: var(--ink-2); margin-top: 1px; }
.t-line { position: relative; height: 2px; width: 420px; max-width: 40vw; margin: 6px auto 0; background: linear-gradient(90deg, transparent, rgba(47, 91, 211, 0.5), transparent);
  i { position: absolute; top: -1px; width: 60px; height: 4px; border-radius: 4px; background: linear-gradient(90deg, transparent, #22c3e6, transparent); animation: scan 3s ease-in-out infinite; } }
@keyframes scan { 0% { left: 0; } 50% { left: calc(100% - 60px); } 100% { left: 0; } }
.top-right { display: flex; justify-content: flex-end; align-items: center; gap: 8px; justify-self: end; flex-wrap: nowrap; white-space: nowrap; }
.port-sel { width: 140px; }
.tbtn { display: inline-flex; align-items: center; gap: 5px; height: 32px; padding: 0 12px; border-radius: 10px; font-size: 13px; cursor: pointer; color: var(--accent); white-space: nowrap;
  background: rgba(255, 255, 255, 0.66); border: 1px solid rgba(255, 255, 255, 0.95); backdrop-filter: blur(12px); box-shadow: 0 4px 14px -8px rgba(47, 91, 211, 0.5); transition: all 0.2s;
  &:hover, &.on { background: #fff; box-shadow: 0 6px 18px -8px rgba(47, 91, 211, 0.7); }
  &.icon { padding: 0 9px; }
  &.primary { color: #fff; background: linear-gradient(135deg, #2f5bd3, #22a6e6); border-color: transparent; &:hover { filter: brightness(1.06); } } }
.seg { display: flex; padding: 2px; border-radius: 9px; background: rgba(47, 91, 211, 0.08); font-size: 12px; font-weight: 500; letter-spacing: 0;
  span { padding: 2px 10px; border-radius: 7px; cursor: pointer; white-space: nowrap; color: var(--ink-2); &:hover { color: var(--accent); } &.on { background: #fff; color: var(--accent); box-shadow: 0 2px 8px -4px rgba(47, 91, 211, 0.6); } } }
.seg.views { flex-shrink: 0; height: 32px; align-items: center; padding: 3px; background: rgba(255, 255, 255, 0.66); border: 1px solid rgba(255, 255, 255, 0.95); backdrop-filter: blur(12px); border-radius: 10px;
  span { font-size: 13px; padding: 3px 10px; } }

/* 主体 */
.body { flex: 1; min-height: 0; display: grid; grid-template-columns: minmax(270px, 22%) minmax(0, 1fr) minmax(320px, 25%); gap: 14px; }
.col { display: flex; flex-direction: column; gap: 12px; min-height: 0; min-width: 0; }
.glass {
  position: relative; padding: 12px 14px; border-radius: 16px;
  background: rgba(255, 255, 255, 0.62); backdrop-filter: blur(18px) saturate(160%); -webkit-backdrop-filter: blur(18px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.85);
  box-shadow: 0 14px 34px -18px rgba(30, 64, 175, 0.45), inset 0 1px 0 rgba(255, 255, 255, 0.95);
  &::before { content: ''; position: absolute; left: 14px; right: 14px; top: 0; height: 1px; background: linear-gradient(90deg, transparent, rgba(34, 195, 230, 0.85), transparent); }
}
.grow { flex: 1; min-height: 0; overflow: hidden; }
.p-title { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 800; color: #1e3a8a; margin-bottom: 10px; letter-spacing: 1px;
  &::before { content: ''; width: 4px; height: 14px; border-radius: 2px; background: linear-gradient(#22c3e6, #2f5bd3); box-shadow: 0 0 8px rgba(34, 195, 230, 0.8); }
  small { font-size: 9px; font-weight: 600; letter-spacing: 2px; color: var(--ink-3); }
  .seg { margin-left: auto; } }
.kpi-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.kpi { position: relative; overflow: hidden; padding: 10px 12px; border-radius: 12px; background: rgba(255, 255, 255, 0.7); border: 1px solid rgba(255, 255, 255, 0.95); --c: #1e3a8a;
  &.u { --c: #2563eb; } &.l { --c: #d97706; } &.t { --c: #0d9488; }
  &::after { content: ''; position: absolute; right: -18px; top: -18px; width: 56px; height: 56px; border-radius: 50%; background: color-mix(in srgb, var(--c) 12%, transparent); }
  .k-label { font-size: 13px; font-weight: 600; color: var(--ink-2); }
  b { display: block; margin: 2px 0; font-size: 24px; font-weight: 800; color: var(--c); line-height: 1.25; white-space: nowrap; small { font-size: 12px; font-weight: 600; color: var(--ink-2); margin-left: 3px; } }
  em { font-style: normal; font-size: 12px; color: var(--ink-2); white-space: nowrap; } }

/* 中间：透出 3D 场景 */
.center { align-items: center; }
.chips { display: flex; gap: 10px; flex-wrap: wrap; justify-content: center; }
.chip { min-width: 112px; padding: 7px 14px; border-radius: 14px; background: rgba(255, 255, 255, 0.66); backdrop-filter: blur(14px); border: 1px solid rgba(255, 255, 255, 0.95);
  box-shadow: 0 10px 24px -16px rgba(30, 64, 175, 0.6);
  span { display: block; font-size: 12px; color: var(--ink-2); }
  b { font-size: 22px; font-weight: 800; &.t { color: #0d9488; } &.w { color: #d97706; } small { font-size: 13px; color: var(--ink-3); font-weight: 600; } } }
.spacer { flex: 1; }
.scene-tip { font-size: 12px; color: var(--ink-2); padding: 4px 14px; border-radius: 999px; background: rgba(255, 255, 255, 0.6); backdrop-filter: blur(8px); }
.trend-panel { width: 100%; max-width: 860px; height: 220px; flex-shrink: 0; display: flex; flex-direction: column; }
.trend-panel :deep(.trend) { flex: 1; min-height: 0; }

.carousel-panel { flex: 2.2; min-height: 0; display: flex; flex-direction: column; }
.carousel-panel :deep(.carousel) { flex: 1; }
.rank-panel { flex: 1; min-height: 0; overflow: hidden; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.loading-box { align-self: center; margin-top: 20vh; padding: 16px 28px; color: var(--ink-2); pointer-events: auto; }

/* 非全屏（含侧边菜单）时可用宽度较小：标题收窄，按钮只留图标，避免换行或挤出 */
.cockpit:not(.is-full) {
  .t-main { font-size: 22px; letter-spacing: 2px; }
  .t-sub { letter-spacing: 4px; }
}
@media (max-width: 1800px) {
  .cockpit:not(.is-full) .tbtn .txt { display: none; }
  .cockpit:not(.is-full) .tbtn:not(.primary) { padding: 0 9px; }
}
@media (max-width: 1500px) {
  .body { grid-template-columns: 260px minmax(0, 1fr) 300px; }
  .cockpit:not(.is-full) .t-main { font-size: 18px; letter-spacing: 1px; }
  .cockpit:not(.is-full) .t-sub { font-size: 9px; letter-spacing: 3px; }
  .cockpit:not(.is-full) .t-line { width: 260px; }
  .top-left .clock { display: none; }
  .rank-panel .p-title small, .carousel-panel .p-title small { display: none; }
}
</style>
