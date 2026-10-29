<template>
  <svg :viewBox="`0 -8 ${VW} 92`" :width="width" :height="width ? (width * 92) / VW : undefined" class="ship" :class="{ sailing }">
    <defs>
      <linearGradient :id="ids.hull" x1="0" x2="0" y1="0" y2="1">
        <stop offset="0" :stop-color="shade(color, 0.15)" />
        <stop offset="1" :stop-color="shade(color, -0.4)" />
      </linearGradient>
      <linearGradient :id="ids.water" x1="0" x2="0" y1="0" y2="1">
        <stop offset="0" stop-color="#7cc0f5" stop-opacity="0.92" />
        <stop offset="1" stop-color="#2b7fd6" stop-opacity="0.95" />
      </linearGradient>
    </defs>

    <g class="vessel">
      <!-- 甲板顶面 -->
      <polygon :points="deckTop" :fill="type === 'TANKER' ? '#b84a3a' : '#9aa7b9'" />
      <polygon :points="deckInner" :fill="type === 'TANKER' ? '#c75a48' : '#b3bfce'" />

      <!-- 上层建筑（驳船无） -->
      <template v-if="type !== 'BARGE'">
        <polygon v-for="(f, i) in house" :key="'hs' + i" :points="f.points" :fill="f.fill" stroke="rgba(0,0,0,0.08)" stroke-width="0.5" />
        <g :transform="zShift(Bd * 0.1)">
          <rect v-for="i in 4" :key="'w' + i" :x="sx + 4 + (i - 1) * ((sw - 10) / 4)" y="30" :width="(sw - 10) / 4 - 2" height="4" rx="1" fill="#4b9de0" />
        </g>
        <g :transform="zShift(Bd * 0.2)">
          <rect :x="sx + 5" y="21" :width="sw - 14" height="3.5" rx="1" fill="#4b9de0" />
        </g>
        <polygon v-for="(f, i) in funnel" :key="'fn' + i" :points="f.points" :fill="f.fill" />
      </template>

      <!-- 货物 / 上层建筑（远到近、下到上绘制，保证遮挡关系正确） -->
      <template v-if="type === 'CONTAINER'">
        <polygon v-for="(f, i) in containerFaces" :key="'c' + i" :points="f.points" :fill="f.fill" stroke="rgba(0,0,0,0.12)" stroke-width="0.4" />
      </template>
      <template v-else-if="type === 'BULK'">
        <template v-for="x in hatches" :key="'h' + x">
          <polygon v-for="(f, i) in box(x, 41, 20, 5, Bd * 0.6, '#5d6d86', Bd * 0.2)" :key="i" :points="f.points" :fill="f.fill" />
          <path :d="heap(x + 2, x + 18, 41, Bd * 0.5, loaded ? 9 : 0)" fill="#3d3a36" />
        </template>
      </template>
      <template v-else-if="type === 'TANKER'">
        <polyline :points="poly([[deckX0, 44, Bd * 0.5], [deckX1, 44, Bd * 0.5]])" stroke="#dfe5ee" stroke-width="2" fill="none" />
        <template v-for="x in hatches" :key="'t' + x">
          <polygon v-for="(f, i) in box(x + 6, 41, 8, 4, 6, '#e3e8ef', Bd * 0.4)" :key="i" :points="f.points" :fill="f.fill" />
        </template>
      </template>
      <template v-else-if="type === 'GENERAL'">
        <template v-for="(x, k) in hatches" :key="'g' + x">
          <polygon v-for="(f, i) in box(x, k % 2 ? 36 : 32, 18, k % 2 ? 10 : 14, Bd * 0.6, k % 2 ? '#d9a65c' : '#8aa1bd', Bd * 0.2)" :key="i" :points="f.points" :fill="f.fill" />
        </template>
        <path v-for="x in cranes" :key="'cr' + x" :d="craneD(x)" stroke="#f0a020" stroke-width="2" fill="none" stroke-linecap="round" />
      </template>
      <template v-else-if="type === 'RORO'">
        <polygon v-for="(f, i) in box(deckX0 - 4, 20, deckX1 - deckX0 + 6, 26, Bd * 0.9, '#f3f6fa', Bd * 0.05)" :key="'ro' + i" :points="f.points" :fill="f.fill" stroke="rgba(0,0,0,0.06)" stroke-width="0.5" />
        <g :transform="zShift(Bd * 0.05)">
          <rect v-for="i in 8" :key="i" :x="deckX0 + (i - 1) * ((deckX1 - deckX0) / 8)" y="26" :width="(deckX1 - deckX0) / 8 - 3" height="4" rx="1" fill="#5aa9e6" />
          <rect :x="deckX0 - 4" y="36" :width="deckX1 - deckX0 + 6" height="3" :fill="color" />
        </g>
      </template>
      <template v-else>
        <path :d="heap(deckX0 - 14, W - 34, 46, Bd * 0.5, loaded ? 16 : 3)" :fill="cargoColor" />
      </template>

      <!-- 近侧船壳 -->
      <polygon :points="hullSide" :fill="`url(#${ids.hull})`" stroke="rgba(0,0,0,0.2)" stroke-width="0.7" />
      <polygon :points="bootStripe" fill="#8b2f2f" opacity="0.9" />
      <polyline :points="sheer" fill="none" stroke="#ffffff" stroke-width="1.4" opacity="0.75" />
      <text v-if="name" :x="W * 0.46" y="56" text-anchor="middle" font-size="6.5" fill="#fff" font-weight="700" opacity="0.92">{{ name }}</text>
    </g>

    <!-- 水面 -->
    <g class="water">
      <path :d="wavePath" :fill="`url(#${ids.water})`" />
      <path :d="wavePath" fill="none" stroke="#ffffff" stroke-opacity="0.5" stroke-width="1" />
    </g>
  </svg>
</template>

<script setup>
import { computed } from 'vue'
import { shade, uid } from '@/utils/color'
import { box, poly, DX, DY } from '@/utils/iso'

const props = defineProps({
  type: { type: String, default: 'BULK' },
  length: { type: [Number, String], default: 90 },
  color: { type: String, default: '#1f6fd1' },
  name: String,
  sailing: Boolean,
  loaded: { type: Boolean, default: true },
  width: Number
})
const ids = { hull: uid('hull'), water: uid('water') }
const Bd = 22
const DECK = 46
const KEEL = 68
const len = computed(() => Math.max(40, Math.min(160, Number(props.length) || 90)))
const W = computed(() => Math.round(110 + len.value * 1.5))
const VW = computed(() => W.value + 18)
const xb = computed(() => W.value * 0.8)
const sx = 12
const sw = 34
const deckX0 = computed(() => (props.type === 'BARGE' ? 20 : sx + sw + 6))
const deckX1 = computed(() => xb.value - 6)

const zShift = (z) => `translate(${z * DX}, ${-z * DY})`
const deckTop = computed(() => poly([[4, DECK, 0], [xb.value, DECK, 0], [W.value - 4, DECK, Bd / 2], [xb.value, DECK, Bd], [4, DECK, Bd]]))
const deckInner = computed(() => poly([[8, DECK, 2], [xb.value - 2, DECK, 2], [W.value - 12, DECK, Bd / 2], [xb.value - 2, DECK, Bd - 2], [8, DECK, Bd - 2]]))
const hullSide = computed(() =>
  poly(props.type === 'BARGE'
    ? [[4, DECK, 0], [xb.value, DECK, 0], [W.value - 4, DECK, Bd / 2], [W.value - 14, KEEL - 4, Bd / 2], [xb.value - 4, KEEL - 4, 0], [10, KEEL - 4, 0]]
    : [[4, DECK, 0], [xb.value, DECK, 0], [W.value - 4, DECK, Bd / 2], [W.value - 26, KEEL, Bd * 0.35], [18, KEEL, 0], [6, DECK + 10, 0]])
)
const bootStripe = computed(() => {
  const k = props.type === 'BARGE' ? KEEL - 4 : KEEL
  return poly([[14, k - 5, 0], [xb.value - 2, k - 5, 0], [W.value - 20, k - 5, Bd * 0.4], [W.value - 26, k, Bd * 0.35], [18, k, 0]])
})
const sheer = computed(() => poly([[5, DECK + 1.5, 0], [xb.value, DECK + 1.5, 0], [W.value - 6, DECK + 1.5, Bd / 2]]))
const hatches = computed(() => {
  const arr = []
  for (let x = deckX0.value + 2; x + 20 <= deckX1.value; x += 24) arr.push(x)
  return arr
})
const cranes = computed(() => hatches.value.filter((_, i) => i % 2 === 0).map((x) => x + 20).slice(0, 3))
const craneD = (x) => {
  const base = poly([[x, DECK, Bd * 0.5], [x, 14, Bd * 0.5], [x + 22, 28, Bd * 0.3]]).split(' ')
  return `M${base[0]} L${base[1]} L${base[2]}`
}
const palette = ['#e67e22', '#1f7ae0', '#27ae60', '#c0392b', '#8e44ad', '#16a085', '#f1c40f']
const containerFaces = computed(() => {
  const faces = []
  let k = 0
  const lanes = [Bd * 0.52, Bd * 0.08]
  lanes.forEach((z, li) => {
    for (let layer = 0; layer < 3; layer++) {
      for (let x = deckX0.value + layer * 2; x + 13 <= deckX1.value - layer * 3; x += 14) {
        if ((k * 7 + li) % 13 === 5 && layer === 2) { k++; continue }
        faces.push(...box(x, DECK - 8.5 * (layer + 1), 13, 8.5, Bd * 0.42, palette[(k * 3 + layer + li) % palette.length], z))
        k++
      }
    }
  })
  return faces
})
const house = computed(() => [...box(sx, 26, sw, 20, Bd * 0.8, '#f5f7fb', Bd * 0.1), ...box(sx + 3, 18, sw - 8, 8, Bd * 0.6, '#ffffff', Bd * 0.2)])
const funnel = computed(() => box(sx + sw / 2 - 3, 6, 6, 12, 6, props.color, Bd * 0.35))
const heap = (x0, x1, y, z, hgt) => {
  const a = poly([[x0, y, z]])
  const b = poly([[x1, y, z]])
  const c = poly([[(x0 + x1) / 2, y - hgt * 1.6, z]])
  return `M${a} Q${c} ${b} Z`
}
const cargoColor = computed(() => '#5b4a3a')
const wavePath = computed(() => {
  const w = VW.value
  let d = 'M0 66'
  for (let x = 0; x <= w + 20; x += 20) d += ' q5 -3 10 0 t10 0'
  return d + ' V84 H0 Z'
})
</script>

<style scoped>
.ship { display: block; overflow: hidden; }
.vessel { animation: float 3.2s ease-in-out infinite; transform-origin: center; }
.sailing .water { animation: drift 2.4s linear infinite; }
@keyframes float { 0%, 100% { transform: translateY(0) rotate(0deg); } 50% { transform: translateY(1.4px) rotate(-0.4deg); } }
@keyframes drift { from { transform: translateX(0); } to { transform: translateX(-20px); } }
</style>
