<template>
  <div class="carousel" @mouseenter="paused = true" @mouseleave="paused = false">
    <div v-if="!berths.length" class="empty">暂无泊位</div>
    <template v-else>
      <div class="nav">
        <button class="arrow" @click="go(-1)"><el-icon><ArrowLeft /></el-icon></button>
        <div class="dots">
          <span v-for="(b, i) in berths" :key="b.id" :class="[b.status, { on: i === index }]" :title="b.berthName" @click="emit('update:index', i)">{{ b.berthCode }}</span>
        </div>
        <button class="arrow" @click="go(1)"><el-icon><ArrowRight /></el-icon></button>
      </div>
      <div class="timer"><i :key="`${index}-${paused}`" :class="{ run: !paused }" :style="{ animationDuration: interval + 'ms' }" /></div>

      <transition name="slide" mode="out-in">
        <div :key="cur.id" class="slide">
          <div class="head" :class="cur.status">
            <div class="code num">{{ cur.berthCode }}</div>
            <div class="flex-1">
              <div class="name">{{ cur.berthName }}</div>
              <div class="sub">作业时间 {{ cur.workStartTime ? cur.workStartTime.slice(0, 5) + ' - ' + cur.workEndTime.slice(0, 5) : '未设置' }} · {{ cur.maxTonnage || '-' }} 吨级</div>
            </div>
            <span class="status"><i class="status-dot" :class="{ pulse: cur.status !== 'IDLE' }" />{{ STATUS[cur.status]?.label }}</span>
          </div>

          <div class="kpis">
            <div><span>累计卸货</span><b class="num u">{{ compact(stat.unloadTonnage) }}</b><small>吨</small></div>
            <div><span>累计装货</span><b class="num l">{{ compact(stat.loadTonnage) }}</b><small>吨</small></div>
            <div><span>累计作业</span><b class="num">{{ stat.workCount || 0 }}</b><small>次</small></div>
            <div><span>运输车次</span><b class="num t">{{ stat.tripCount || 0 }}</b><small>车</small></div>
          </div>

          <div class="block-title">货物类型统计</div>
          <CargoTypeBars :data="stat.cargoTypes || []" :limit="2" dense />

          <div class="block-title">
            当前作业
            <span class="chips">
              <em>待装载 {{ cur.waitingCount || 0 }}</em>
              <em class="t">运输中 {{ cur.inTransitCount || 0 }}</em>
            </span>
          </div>
          <div v-if="loading && !detail" class="muted small">加载中…</div>
          <div v-else-if="!activeCargos.length" class="idle">{{ cur.status === 'MAINTENANCE' ? '泊位维修中，暂停作业' : '当前没有作业货物' }}</div>
          <div v-for="c in activeCargos" :key="c.id" class="work">
            <div class="w-top">
              <span class="dir" :class="c.direction">{{ c.direction === 'LOAD' ? '装货' : '卸货' }}</span>
              <b class="w-name">{{ c.cargoName }}</b>
              <span class="muted small">{{ c.cargoTypeName }}</span>
              <span class="num w-pct">{{ percent(c.loadedTonnage, c.tonnage) }}%</span>
            </div>
            <div class="w-bar"><span :class="c.direction" :style="{ width: percent(c.loadedTonnage, c.tonnage) + '%' }" /></div>
            <div class="w-meta">
              <span class="num">{{ fmtNum(c.loadedTonnage, 1) }} / {{ fmtNum(c.tonnage, 1) }} 吨</span>
              <span class="ships">
                <span v-for="s in (c.ships || []).slice(0, 2)" :key="s.id" class="ship"><ShipSvg :type="s.shipType" :length="s.shipLength" :color="s.hullColor || '#1f6fd1'" :width="34" />{{ s.shipName }}</span>
              </span>
            </div>
          </div>
        </div>
      </transition>
    </template>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import ShipSvg from '@/components/svg/ShipSvg.vue'
import CargoTypeBars from './CargoTypeBars.vue'
import { STATUS, compact } from './palette'
import { fmtNum, percent } from '@/utils/format'
import { berthDetail } from '@/api/berth'

/** 泊位轮播：每隔 interval 毫秒切换，鼠标悬停暂停 */
const props = defineProps({
  berths: { type: Array, default: () => [] },
  stats: { type: Object, default: () => ({}) },
  index: { type: Number, default: 0 },
  interval: { type: Number, default: 9000 }
})
const emit = defineEmits(['update:index'])
const paused = ref(false)
const detail = ref(null)
const loading = ref(false)
const cache = new Map()

const cur = computed(() => props.berths[props.index] || props.berths[0] || {})
const stat = computed(() => props.stats[cur.value.id] || {})
const activeCargos = computed(() => (detail.value?.berth.id === cur.value.id ? detail.value.cargos : []).slice(0, 3))

function go(step) {
  const n = props.berths.length
  if (n) emit('update:index', (props.index + step + n) % n)
}
async function loadDetail() {
  const id = cur.value.id
  if (!id) return
  const hit = cache.get(id)
  if (hit && Date.now() - hit.t < 30000) {
    detail.value = hit.d
    return
  }
  loading.value = true
  try {
    const d = await berthDetail(id)
    cache.set(id, { t: Date.now(), d })
    if (cur.value.id === id) detail.value = d
  } catch (e) {
    detail.value = null
  } finally {
    loading.value = false
  }
}

let timer = null
function schedule() {
  clearTimeout(timer)
  if (!paused.value) timer = setTimeout(() => go(1), props.interval)
}
watch(() => cur.value.id, loadDetail, { immediate: true })
watch(() => [props.index, paused.value, props.berths.length], schedule, { immediate: true })
onBeforeUnmount(() => clearTimeout(timer))
defineExpose({ refresh: () => cache.clear() || loadDetail() })
</script>

<style scoped lang="scss">
.carousel { display: flex; flex-direction: column; height: 100%; min-height: 0; }
.empty, .idle { font-size: 13px; color: #6b7a92; padding: 14px 0; text-align: center; }
.nav { display: flex; align-items: center; gap: 6px; }
.arrow { width: 26px; height: 26px; border-radius: 8px; border: 1px solid rgba(47, 91, 211, 0.2); background: rgba(255, 255, 255, 0.6); color: #2f5bd3; cursor: pointer; display: grid; place-items: center;
  &:hover { background: #2f5bd3; color: #fff; } }
.dots { flex: 1; display: flex; gap: 4px; overflow: hidden;
  span { flex: 1; min-width: 0; text-align: center; font-size: 11px; font-weight: 700; padding: 3px 0; border-radius: 6px; cursor: pointer; color: #3b4a63;
    background: rgba(255, 255, 255, 0.55); border-bottom: 2px solid #12b886; transition: all 0.2s;
    &.WORKING { border-bottom-color: #f59f00; } &.MAINTENANCE { border-bottom-color: #e5484d; }
    &.on { background: linear-gradient(135deg, #2f5bd3, #22c3e6); color: #fff; box-shadow: 0 4px 12px -4px rgba(47, 91, 211, 0.7); } } }
.timer { height: 2px; margin: 8px 0 10px; background: rgba(47, 91, 211, 0.1); border-radius: 2px; overflow: hidden;
  i { display: block; height: 100%; width: 0; background: linear-gradient(90deg, #22c3e6, #2f5bd3); &.run { animation: fill linear forwards; } } }
@keyframes fill { to { width: 100%; } }
.slide { flex: 1; min-height: 0; overflow-x: hidden; overflow-y: auto; display: flex; flex-direction: column; scrollbar-width: thin; scrollbar-color: rgba(47, 91, 211, 0.25) transparent;
  &::-webkit-scrollbar { width: 4px; } &::-webkit-scrollbar-thumb { background: rgba(47, 91, 211, 0.25); border-radius: 4px; } }
.head { flex-shrink: 0; display: flex; align-items: center; gap: 12px; padding: 10px 12px; border-radius: 14px; color: #fff; --c1: #12b886; --c2: #0b7a5a;
  background: linear-gradient(120deg, var(--c2), var(--c1)); box-shadow: 0 10px 24px -14px var(--c1);
  &.WORKING { --c1: #f5a524; --c2: #d97706; } &.MAINTENANCE { --c1: #f06469; --c2: #b4232a; } }
.code { font-size: 26px; font-weight: 800; padding: 2px 10px; border-radius: 10px; background: rgba(255, 255, 255, 0.2); }
.flex-1 { flex: 1; min-width: 0; }
.name { font-size: 17px; font-weight: 700; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.sub { font-size: 12px; opacity: 0.88; margin-top: 2px; white-space: nowrap; }
.status { display: flex; align-items: center; gap: 6px; font-size: 12px; padding: 3px 10px; border-radius: 999px; background: rgba(255, 255, 255, 0.24); white-space: nowrap;
  .status-dot { background: #fff; color: #fff; } }
.kpis { display: grid; grid-template-columns: repeat(4, 1fr); gap: 6px; margin: 8px 0 2px; flex-shrink: 0;
  div { padding: 7px 8px; border-radius: 10px; background: rgba(255, 255, 255, 0.6); border: 1px solid rgba(255, 255, 255, 0.85); display: flex; flex-direction: column; }
  span { font-size: 11px; color: #6b7a92; } b { font-size: 18px; color: #142033; line-height: 1.25; &.u { color: #2563eb; } &.l { color: #d97706; } &.t { color: #0d9488; } }
  small { font-size: 10px; color: #6b7a92; } }
.block-title { flex-shrink: 0; display: flex; align-items: center; gap: 8px; font-size: 12px; font-weight: 700; color: #1e3a8a; letter-spacing: 1px; margin: 8px 0 5px;
  &::before { content: ''; width: 3px; height: 11px; border-radius: 2px; background: linear-gradient(#22c3e6, #2f5bd3); } }
.chips { margin-left: auto; display: flex; gap: 6px; em { font-style: normal; font-weight: 500; font-size: 11px; padding: 1px 8px; border-radius: 999px; background: #fff4de; color: #b45309; &.t { background: #e6f6f4; color: #0d9488; } } }
.work { flex-shrink: 0; padding: 7px 10px; border-radius: 12px; background: rgba(255, 255, 255, 0.62); border: 1px solid rgba(255, 255, 255, 0.85); & + .work { margin-top: 6px; } }
.w-top { display: flex; align-items: center; gap: 6px; font-size: 13px; }
.dir { font-size: 11px; padding: 0 6px; border-radius: 5px; color: #2563eb; background: #e8f0fe; &.LOAD { color: #b45309; background: #fff4de; } }
.w-name { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 150px; }
.w-pct { margin-left: auto; font-weight: 700; color: #1e3a8a; }
.w-bar { height: 5px; border-radius: 4px; background: rgba(47, 91, 211, 0.08); margin: 6px 0 4px; overflow: hidden;
  span { display: block; height: 100%; border-radius: 4px; background: #2563eb; transition: width 0.8s; &.LOAD { background: #d97706; } } }
.w-meta { display: flex; align-items: center; justify-content: space-between; gap: 8px; font-size: 11px; color: #3b4a63; }
.ships { display: flex; gap: 8px; overflow: hidden; }
.ship { display: flex; align-items: center; gap: 3px; white-space: nowrap; }
.small { font-size: 12px; }
.muted { color: #6b7a92; }
.slide-enter-active, .slide-leave-active { transition: all 0.45s cubic-bezier(0.2, 0.8, 0.2, 1); }
.slide-enter-from { opacity: 0; transform: translateX(24px); }
.slide-leave-to { opacity: 0; transform: translateX(-24px); }
</style>
