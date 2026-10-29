<template>
  <div class="trend">
    <div class="legend">
      <span v-for="s in series" :key="s.key"><i :style="{ background: s.color }" />{{ s.label }}</span>
      <span class="unit">单位：吨</span>
    </div>
    <div ref="boxRef" class="plot" @mousemove="onMove" @mouseleave="hoverIdx = -1">
      <svg :width="W" :height="H">
        <g class="grid">
          <line v-for="t in ticks" :key="t" :x1="PL" :x2="W - PR" :y1="y(t)" :y2="y(t)" />
          <text v-for="t in ticks" :key="'t' + t" :x="PL - 6" :y="y(t) + 4" text-anchor="end">{{ compact(t) }}</text>
        </g>
        <defs>
          <linearGradient v-for="s in series" :id="`ga-${s.key}`" :key="s.key" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0" :stop-color="s.color" stop-opacity="0.18" />
            <stop offset="1" :stop-color="s.color" stop-opacity="0" />
          </linearGradient>
        </defs>
        <path v-for="s in series" :key="'a' + s.key" :d="area(s.key)" :fill="`url(#ga-${s.key})`" />
        <path v-for="s in series" :key="'l' + s.key" :d="line(s.key)" :stroke="s.color" class="ln" />
        <text v-for="(d, i) in data" :key="d.day" :x="x(i)" :y="H - 6" class="xl" text-anchor="middle">{{ d.day.slice(5) }}</text>
        <g v-if="hoverIdx >= 0">
          <line :x1="x(hoverIdx)" :x2="x(hoverIdx)" :y1="PT" :y2="H - PB" class="cross" />
          <circle v-for="s in series" :key="'c' + s.key" :cx="x(hoverIdx)" :cy="y(val(hoverIdx, s.key))" r="4.5" :fill="s.color" class="dot" />
        </g>
      </svg>
      <div v-if="hoverIdx >= 0" class="tip" :style="tipStyle">
        <b>{{ data[hoverIdx].day }}</b>
        <div v-for="s in series" :key="s.key"><i :style="{ background: s.color }" />{{ s.label }}<span class="num">{{ fmtNum(val(hoverIdx, s.key), 1) }} 吨</span></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { SERIES, compact } from './palette'
import { fmtNum } from '@/utils/format'

/** 近7日装卸趋势：单一吨数坐标轴，三条线（卸货、装货、车辆运出） */
const props = defineProps({ data: { type: Array, default: () => [] } })
const series = [
  { key: 'unloadTonnage', ...SERIES.unload },
  { key: 'loadTonnage', ...SERIES.load },
  { key: 'tripTonnage', ...SERIES.trip }
]
const boxRef = ref()
const W = ref(600)
const H = ref(160)
const PL = 44
const PR = 14
const PT = 10
const PB = 24
const hoverIdx = ref(-1)
let ro = null

const val = (i, k) => Number(props.data[i]?.[k]) || 0
const rawMax = computed(() => Math.max(1, ...props.data.flatMap((d) => series.map((s) => Number(d[s.key]) || 0))))
const niceMax = computed(() => {
  const m = rawMax.value
  const p = Math.pow(10, Math.floor(Math.log10(m)))
  return Math.ceil(m / p / 2) * 2 * p
})
const ticks = computed(() => [0, 0.25, 0.5, 0.75, 1].map((f) => Math.round(niceMax.value * f)))
const x = (i) => PL + (props.data.length > 1 ? (i / (props.data.length - 1)) * (W.value - PL - PR) : 0)
const y = (v) => PT + (1 - v / niceMax.value) * (H.value - PT - PB)
const line = (k) => props.data.map((d, i) => `${i ? 'L' : 'M'}${x(i).toFixed(1)},${y(val(i, k)).toFixed(1)}`).join('')
const area = (k) => (props.data.length ? `${line(k)}L${x(props.data.length - 1)},${y(0)}L${x(0)},${y(0)}Z` : '')
const tipStyle = computed(() => {
  const left = x(hoverIdx.value)
  return left > W.value * 0.6 ? { right: `${W.value - left + 12}px` } : { left: `${left + 12}px` }
})

function onMove(e) {
  if (!props.data.length) return
  const rect = boxRef.value.getBoundingClientRect()
  const px = e.clientX - rect.left
  const step = (W.value - PL - PR) / Math.max(1, props.data.length - 1)
  hoverIdx.value = Math.max(0, Math.min(props.data.length - 1, Math.round((px - PL) / step)))
}
onMounted(() => {
  ro = new ResizeObserver(() => {
    W.value = boxRef.value.clientWidth || 600
    H.value = boxRef.value.clientHeight || 160
  })
  ro.observe(boxRef.value)
})
onBeforeUnmount(() => ro?.disconnect())
</script>

<style scoped lang="scss">
.trend { display: flex; flex-direction: column; height: 100%; }
.legend { display: flex; gap: 14px; font-size: 12px; color: #3b4a63; margin-bottom: 4px;
  i { display: inline-block; width: 14px; height: 2px; border-radius: 1px; margin-right: 5px; vertical-align: middle; }
  .unit { margin-left: auto; color: #6b7a92; } }
.plot { position: relative; flex: 1; min-height: 110px; min-width: 0; overflow: hidden; }
svg { position: absolute; left: 0; top: 0; display: block; }
.grid line { stroke: rgba(47, 91, 211, 0.1); stroke-dasharray: 3 4; }
.grid text, .xl { font-size: 10px; fill: #6b7a92; }
.ln { fill: none; stroke-width: 2; stroke-linejoin: round; stroke-linecap: round; }
.cross { stroke: rgba(47, 91, 211, 0.45); stroke-width: 1; }
.dot { stroke: #fff; stroke-width: 2; }
.tip { position: absolute; top: 6px; padding: 8px 10px; border-radius: 10px; font-size: 12px; color: #142033; pointer-events: none; white-space: nowrap;
  background: rgba(255, 255, 255, 0.92); backdrop-filter: blur(10px); box-shadow: 0 10px 24px -10px rgba(15, 35, 102, 0.4); border: 1px solid rgba(255, 255, 255, 0.9);
  div { display: flex; align-items: center; gap: 6px; margin-top: 3px; }
  i { width: 8px; height: 8px; border-radius: 2px; }
  span { margin-left: auto; padding-left: 14px; font-weight: 700; } }
</style>
