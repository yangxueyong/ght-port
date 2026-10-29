<template>
  <div class="rank">
    <div v-if="mode === 'shipper'" class="legend">
      <span><i :style="{ background: SERIES.unload.color }" />卸货</span>
      <span><i :style="{ background: SERIES.load.color }" />装货</span>
      <span class="unit">单位：吨</span>
    </div>
    <div v-else class="legend"><span><i :style="{ background: SERIES.trip.color }" />累计运输吨数</span><span class="unit">单位：吨</span></div>
    <div v-if="!rows.length" class="empty">暂无数据</div>
    <div v-for="(r, i) in rows" :key="r.id" class="row" :title="tipText(r)">
      <span class="no" :class="'n' + (i + 1)">{{ i + 1 }}</span>
      <div class="main">
        <div class="top">
          <b class="nm">{{ mode === 'shipper' ? r.companyName : r.carrierName }}</b>
          <span class="meta">{{ meta(r) }}</span>
          <span class="val num">{{ compact(total(r)) }}</span>
        </div>
        <div class="bar">
          <template v-if="mode === 'shipper'">
            <span :style="{ width: w(r.unloadTonnage), background: SERIES.unload.color }" />
            <span :style="{ width: w(r.loadTonnage), background: SERIES.load.color }" />
          </template>
          <span v-else :style="{ width: w(r.stat?.totalTonnage), background: SERIES.trip.color }" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { SERIES, compact } from './palette'
import { fmtNum } from '@/utils/format'

/** 排行：carrier 承运商（按运输吨数）| shipper 船运公司（卸货+装货，堆叠） */
const props = defineProps({ data: { type: Array, default: () => [] }, mode: { type: String, default: 'carrier' }, limit: { type: Number, default: 5 } })
const total = (r) => (props.mode === 'shipper' ? (Number(r.unloadTonnage) || 0) + (Number(r.loadTonnage) || 0) : Number(r.stat?.totalTonnage) || 0)
const rows = computed(() => props.data.slice(0, props.limit))
const max = computed(() => Math.max(1, ...rows.value.map(total)))
const w = (v) => `${((Number(v) || 0) / max.value) * 100}%`
const meta = (r) =>
  props.mode === 'shipper'
    ? `${r.shipCount || 0} 艘船${r.workingShipCount ? ` · ${r.workingShipCount} 艘作业中` : ''}`
    : `${r.vehicleCount || 0} 车 · ${r.stat?.tripCount || 0} 车次${r.inTransitVehicleCount ? ` · ${r.inTransitVehicleCount} 运输中` : ''}`
const tipText = (r) =>
  props.mode === 'shipper'
    ? `${r.companyName}\n卸货 ${fmtNum(r.unloadTonnage, 1)} 吨 / ${fmtNum(r.unloadVolume, 1)} 方\n装货 ${fmtNum(r.loadTonnage, 1)} 吨 / ${fmtNum(r.loadVolume, 1)} 方`
    : `${r.carrierName}\n运输 ${r.stat?.tripCount || 0} 车次，${fmtNum(r.stat?.totalTonnage, 1)} 吨 / ${fmtNum(r.stat?.totalVolume, 1)} 方`
</script>

<style scoped lang="scss">
.rank { display: flex; flex-direction: column; gap: 8px; }
.legend { display: flex; gap: 14px; font-size: 12px; color: #3b4a63;
  i { display: inline-block; width: 10px; height: 4px; border-radius: 2px; margin-right: 5px; vertical-align: middle; }
  .unit { margin-left: auto; color: #6b7a92; } }
.empty { font-size: 13px; color: #6b7a92; padding: 14px 0; text-align: center; }
.row { display: flex; align-items: center; gap: 10px; }
.no { width: 20px; height: 20px; border-radius: 6px; display: grid; place-items: center; font-size: 11px; font-weight: 800; color: #3b4a63; background: rgba(47, 91, 211, 0.08); flex-shrink: 0;
  &.n1 { color: #fff; background: linear-gradient(135deg, #2f5bd3, #22c3e6); } &.n2 { color: #fff; background: #4f7fe8; } &.n3 { color: #fff; background: #8fb1f3; } }
.main { flex: 1; min-width: 0; }
.top { display: flex; align-items: baseline; gap: 6px; font-size: 13px; }
.nm { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: #142033; }
.meta { font-size: 11px; color: #6b7a92; white-space: nowrap; }
.val { margin-left: auto; font-weight: 700; color: #142033; }
.bar { display: flex; gap: 2px; height: 5px; margin-top: 4px; border-radius: 4px; background: rgba(47, 91, 211, 0.07); overflow: hidden;
  span { height: 100%; border-radius: 4px; transition: width 0.8s cubic-bezier(0.2, 0.8, 0.2, 1); } }
</style>
