<template>
  <svg :viewBox="`-2 -12 ${VW} 74`" :width="width" :height="width ? (width * 74) / VW : undefined" class="truck" :class="{ moving }">
    <defs>
      <linearGradient :id="ids.tank" x1="0" x2="0" y1="0" y2="1">
        <stop offset="0" stop-color="#f6f8fb" />
        <stop offset="0.35" stop-color="#d5dce6" />
        <stop offset="1" stop-color="#7e8a9c" />
      </linearGradient>
      <linearGradient :id="ids.glass" x1="0" x2="1" y1="0" y2="1">
        <stop offset="0" stop-color="#d9f0ff" />
        <stop offset="1" stop-color="#5fa8e6" />
      </linearGradient>
    </defs>

    <!-- 地面投影 -->
    <polygon :points="shadow" fill="rgba(20,32,51,0.16)" />
    <g v-if="moving" class="speed" stroke="#a9b4c6" stroke-linecap="round">
      <line x1="-2" y1="20" x2="8" y2="20" />
      <line x1="0" y1="30" x2="12" y2="30" />
      <line x1="-2" y1="40" x2="6" y2="40" />
    </g>

    <!-- 远侧车轮 -->
    <g v-for="(x, i) in wheels" :key="'fw' + i">
      <ellipse :cx="P(x, 49, D)[0]" :cy="P(x, 49, D)[1]" rx="6.6" ry="6.9" fill="#11161f" />
    </g>

    <g class="body">
      <!-- 底盘 -->
      <polygon v-for="(f, i) in chassis" :key="'ch' + i" :points="f.points" :fill="f.fill" />

      <!-- 厢式 / 半挂 / 集装箱 -->
      <template v-if="kind === 'box'">
        <polygon v-for="(f, i) in cargoBox" :key="'cb' + i" :points="f.points" :fill="f.fill" stroke="rgba(0,0,0,0.08)" stroke-width="0.6" />
        <template v-if="type === 'CONTAINER'">
          <line v-for="x in ribs(bx0 + 4, bx1 - 6, 5)" :key="'r' + x" :x1="x" :y1="boxTop + 2" :x2="x" y2="41" :stroke="shade(color, -0.22)" stroke-width="0.9" />
          <text :x="bx0 + 8" y="31" font-size="8" font-weight="800" fill="rgba(255,255,255,0.75)" font-family="Arial">GHT</text>
        </template>
        <template v-else>
          <rect :x="bx0" y="33" :width="bx1 - bx0" height="4" :fill="color" opacity="0.9" />
          <polygon :points="poly([[bx1, 33, 0], [bx1, 33, D], [bx1, 37, D], [bx1, 37, 0]])" :fill="shade(color, -0.25)" />
          <line :x1="bx0 + 4" :y1="boxTop + 2" :x2="bx0 + 4" y2="42" stroke="rgba(0,0,0,0.12)" stroke-width="0.8" />
        </template>
      </template>

      <!-- 罐式车：圆角罐体 -->
      <template v-else-if="kind === 'tank'">
        <polygon :points="poly([[bx0 + 8, 16, 0], [bx1 - 8, 16, 0], [bx1 - 8, 16, D], [bx0 + 8, 16, D]])" fill="#eef2f7" />
        <polygon :points="poly([[bx1, 24, 0], [bx1, 24, D], [bx1, 36, D], [bx1, 36, 0]])" fill="#8a96a8" />
        <rect :x="bx0" y="16" :width="bx1 - bx0" height="27" rx="11" :fill="`url(#${ids.tank})`" stroke="#7e8a9c" stroke-width="0.7" />
        <line v-for="x in ribs(bx0 + 14, bx1 - 12, 18)" :key="'t' + x" :x1="x" y1="17" :x2="x" y2="42" stroke="#98a4b6" stroke-width="1.1" />
        <polygon v-for="(f, i) in box((bx0 + bx1) / 2 - 6, 11, 12, 5, 8, '#8c97a8', D / 2 - 4)" :key="'hd' + i" :points="f.points" :fill="f.fill" />
        <path :d="`M${bx0 + 8} 30 H${bx1 - 8}`" :stroke="color" stroke-width="3" />
      </template>

      <!-- 自卸车：梯形车斗 -->
      <template v-else-if="kind === 'dump'">
        <polygon :points="poly([[bx0, 18, 0], [bx1, 18, 0], [bx1, 18, D], [bx0, 18, D]])" :fill="shade(color, -0.45)" />
        <path v-if="loaded" :d="heapPath" fill="#4a3b2e" />
        <polygon :points="poly([[bx1, 18, 0], [bx1, 18, D], [bx1 - 3, 43, D], [bx1 - 3, 43, 0]])" :fill="shade(color, -0.25)" />
        <polygon :points="`${bx0},18 ${bx1},18 ${bx1 - 3},43 ${bx0 + 7},43`" :fill="color" stroke="rgba(0,0,0,0.12)" stroke-width="0.6" />
        <path v-for="x in ribs(bx0 + 9, bx1 - 6, 10)" :key="'d' + x" :d="`M${x} 20 V41`" stroke="rgba(0,0,0,0.16)" stroke-width="1" />
      </template>

      <!-- 平板车：钢卷 -->
      <template v-else-if="kind === 'flat'">
        <polygon v-for="(f, i) in box(bx0, 37, bx1 - bx0, 6, D, shade(color, -0.1))" :key="'fp' + i" :points="f.points" :fill="f.fill" />
        <template v-if="loaded">
          <g v-for="(x, i) in ribs(bx0 + 12, bx1 - 10, 22)" :key="'coil' + i">
            <circle :cx="P(x, 28, D * 0.85)[0]" :cy="P(x, 28, D * 0.85)[1]" r="9" fill="#6b788c" />
            <polygon :points="coilBody(x)" fill="#8795aa" />
            <circle :cx="x" cy="28" r="9" fill="#a6b2c4" stroke="#6b788c" stroke-width="1" />
            <circle :cx="x" cy="28" r="3.2" fill="#5b677a" />
          </g>
        </template>
      </template>

      <!-- 重型栏板 -->
      <template v-else>
        <polygon v-for="(f, i) in box(bx0, 36, bx1 - bx0, 7, D, shade(color, -0.08))" :key="'hp' + i" :points="f.points" :fill="f.fill" />
        <polyline :points="poly([[bx0, 36, D], [bx0, 22, D], [bx1, 22, D], [bx1, 36, D]])" fill="none" :stroke="shade(color, -0.3)" stroke-width="1.6" />
        <template v-if="loaded">
          <polygon v-for="(f, i) in box(bx0 + 5, 18, (bx1 - bx0) * 0.55, 18, D * 0.7, '#c9a26b', 2)" :key="'cr' + i" :points="f.points" :fill="f.fill" stroke="rgba(0,0,0,0.12)" stroke-width="0.5" />
        </template>
        <rect :x="bx0" y="22" :width="bx1 - bx0" height="14" fill="none" :stroke="shade(color, -0.12)" stroke-width="2" />
        <line v-for="x in ribs(bx0 + 8, bx1 - 4, 9)" :key="'hr' + x" :x1="x" y1="22" :x2="x" y2="36" :stroke="shade(color, -0.12)" stroke-width="1.4" />
        <polyline :points="poly([[bx1, 22, 0], [bx1, 22, D], [bx1, 36, D]])" fill="none" :stroke="shade(color, -0.25)" stroke-width="1.6" />
      </template>

      <!-- 车头：顶面、风挡斜面、前脸、侧面 -->
      <polygon :points="cab.top" :fill="shade(color, 0.3)" />
      <polygon :points="cab.glass" :fill="`url(#${ids.glass})`" />
      <polygon :points="cab.front" :fill="shade(color, -0.25)" />
      <polygon :points="cab.side" :fill="color" stroke="rgba(0,0,0,0.1)" stroke-width="0.6" />
      <polygon :points="cab.window" fill="#bfe3ff" opacity="0.95" />
      <polygon :points="cab.shine" fill="#ffffff" opacity="0.55" />
      <polygon :points="cab.grille" fill="#2b3445" opacity="0.75" />
      <polygon :points="cab.light" fill="#ffd166" />
      <rect :x="cx0 + 3" y="38" width="8" height="1.6" rx="0.8" fill="rgba(0,0,0,0.35)" />
    </g>

    <!-- 近侧车轮 -->
    <g v-for="(x, i) in wheels" :key="'w' + i">
      <ellipse :cx="P(x, 49, 2.2)[0]" :cy="P(x, 49, 2.2)[1]" rx="7" ry="7.3" fill="#0d1118" />
      <g class="wheel" :style="{ transformOrigin: `${x}px 49px` }">
        <circle :cx="x" cy="49" r="7.2" fill="#1d2433" />
        <circle :cx="x" cy="49" r="3.8" fill="#c8d1de" />
        <line :x1="x - 3.4" y1="49" :x2="x + 3.4" y2="49" stroke="#1d2433" stroke-width="1.1" />
        <line :x1="x" y1="45.6" :x2="x" y2="52.4" stroke="#1d2433" stroke-width="1.1" />
      </g>
    </g>
  </svg>
</template>

<script setup>
import { computed } from 'vue'
import { shade, uid } from '@/utils/color'
import { box, poly, proj } from '@/utils/iso'

const props = defineProps({
  type: { type: String, default: 'HEAVY_CARGO' },
  length: { type: [Number, String], default: 9.6 },
  color: { type: String, default: '#1f7ae0' },
  moving: Boolean,
  loaded: { type: Boolean, default: true },
  width: Number
})

const ids = { tank: uid('tank'), glass: uid('glass') }
const P = proj
const D = 18
const len = computed(() => Math.max(3.5, Math.min(18, Number(props.length) || 9.6)))
const W = computed(() => Math.round(64 + len.value * 11))
const VW = computed(() => W.value + 16)
const tractor = computed(() => ['SEMI_TRAILER', 'CONTAINER', 'FLATBED'].includes(props.type))
const kind = computed(() => {
  if (['LIGHT_VAN', 'MEDIUM_BOX', 'SEMI_TRAILER', 'CONTAINER'].includes(props.type)) return 'box'
  if (props.type === 'TANKER') return 'tank'
  if (props.type === 'DUMP') return 'dump'
  if (props.type === 'FLATBED') return 'flat'
  return 'rail'
})
const cabW = computed(() => (props.type === 'LIGHT_VAN' ? 30 : 36))
const cx0 = computed(() => W.value - cabW.value - 4)
const cabTop = computed(() => (props.type === 'LIGHT_VAN' ? 22 : 15))
const bx0 = 6
const bx1 = computed(() => cx0.value - (tractor.value ? 4 : 2))
const boxTop = computed(() => ({ LIGHT_VAN: 20, MEDIUM_BOX: 12, SEMI_TRAILER: 10, CONTAINER: 11 })[props.type] || 12)

const cargoBox = computed(() => {
  const c = props.type === 'CONTAINER' ? props.color : '#f4f6fa'
  return box(bx0, boxTop.value, bx1.value - bx0, 43 - boxTop.value, D, c)
})
const chassis = computed(() => box(bx0, 43, W.value - bx0 - 6, 5, D, '#2b3445'))

const cab = computed(() => {
  const x0 = cx0.value
  const t = cabTop.value
  const x1 = W.value - 4
  const xs = x1 - 10
  const ys = t + 13
  return {
    side: `${x0},46 ${x0},${t} ${xs},${t} ${x1},${ys} ${x1},46`,
    top: poly([[x0, t, 0], [xs, t, 0], [xs, t, D], [x0, t, D]]),
    glass: poly([[xs, t, 0], [x1, ys, 0], [x1, ys, D], [xs, t, D]]),
    front: poly([[x1, ys, 0], [x1, 46, 0], [x1, 46, D], [x1, ys, D]]),
    window: `${x1 - 18},${t + 3} ${x1 - 11.5},${t + 3} ${x1 - 3},${t + 14} ${x1 - 18},${t + 14}`,
    shine: `${x1 - 16},${t + 4} ${x1 - 13},${t + 4} ${x1 - 15},${t + 12} ${x1 - 16},${t + 12}`,
    grille: poly([[x1, ys + 6, 4], [x1, ys + 14, 4], [x1, ys + 14, D - 4], [x1, ys + 6, D - 4]]),
    light: poly([[x1, 39, 1.5], [x1, 43, 1.5], [x1, 43, 5], [x1, 39, 5]])
  }
})
const shadow = computed(() => poly([[1, 56, 0], [W.value, 56, 0], [W.value + 2, 56, D + 2], [3, 56, D + 2]]))
const wheels = computed(() => {
  const front = W.value - 18
  const rear = []
  if (len.value < 6) rear.push(bx0 + 16)
  else if (len.value < 10) rear.push(bx0 + 16, bx0 + 32)
  else rear.push(bx0 + 14, bx0 + 30, bx0 + 46)
  if (tractor.value) rear.push(cx0.value - 4)
  return [...rear, front]
})
const heapPath = computed(() => {
  const [ax, ay] = proj(bx0 + 2, 18, D / 2)
  const [bxp, byp] = proj(bx1.value - 2, 18, D / 2)
  const [cx, cy] = proj((bx0 + bx1.value) / 2, 4, D / 2)
  return `M${ax} ${ay} Q${cx} ${cy - 8} ${bxp} ${byp} Z`
})
const coilBody = (x) => {
  const a = proj(x, 19, 0)
  const b = proj(x, 19, D * 0.85)
  const c = proj(x, 37, D * 0.85)
  const d = proj(x, 37, 0)
  return [a, b, c, d].map((p) => p.join(',')).join(' ')
}
const ribs = (from, to, step) => {
  const arr = []
  for (let x = from; x <= to; x += step) arr.push(x)
  return arr
}
</script>

<style scoped>
.truck { display: block; overflow: visible; }
.moving .wheel { animation: spin 0.6s linear infinite; }
.moving .body { animation: bob 0.5s ease-in-out infinite alternate; }
.speed line { stroke-width: 2; animation: dash 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes bob { from { transform: translateY(0); } to { transform: translateY(-0.8px); } }
@keyframes dash { 0% { opacity: 0; transform: translateX(6px); } 50% { opacity: 1; } 100% { opacity: 0; transform: translateX(-6px); } }
</style>
