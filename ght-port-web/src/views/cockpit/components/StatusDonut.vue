<template>
  <div class="donut-wrap">
    <svg viewBox="0 0 120 120" class="donut">
      <circle cx="60" cy="60" :r="R" class="track" />
      <circle v-for="s in segs" :key="s.key" cx="60" cy="60" :r="R" class="seg" :stroke="s.color"
        :stroke-dasharray="`${s.len} ${C - s.len}`" :stroke-dashoffset="-s.offset" @mouseenter="hover = s" @mouseleave="hover = null">
        <title>{{ s.label }} {{ s.count }} 个</title>
      </circle>
      <text x="60" y="58" class="c-val">{{ hover ? hover.count : total }}</text>
      <text x="60" y="76" class="c-label">{{ hover ? hover.label : '泊位总数' }}</text>
    </svg>
    <div class="legend">
      <div v-for="s in list" :key="s.key" class="lg" :class="{ dim: hover && hover.key !== s.key }" @mouseenter="hover = s" @mouseleave="hover = null">
        <i :style="{ background: s.color }" />
        <span>{{ s.label }}</span>
        <b class="num">{{ s.count }}</b>
        <small>{{ total ? Math.round((s.count / total) * 100) : 0 }}%</small>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { STATUS } from './palette'

const props = defineProps({ berths: { type: Array, default: () => [] } })
const R = 46
const C = 2 * Math.PI * R
const GAP = 2.5
const hover = ref(null)
const total = computed(() => props.berths.length)
const list = computed(() => Object.entries(STATUS).map(([key, s]) => ({ key, ...s, count: props.berths.filter((b) => b.status === key).length })))
const segs = computed(() => {
  let offset = 0
  return list.value
    .filter((s) => s.count > 0)
    .map((s) => {
      const full = (s.count / (total.value || 1)) * C
      const seg = { ...s, len: Math.max(0.5, full - GAP), offset }
      offset += full
      return seg
    })
})
</script>

<style scoped lang="scss">
.donut-wrap { display: flex; align-items: center; gap: 14px; }
.legend { min-width: 0; }
.donut { width: 116px; height: 116px; flex-shrink: 0; transform: rotate(-90deg); }
.track { fill: none; stroke: rgba(47, 91, 211, 0.08); stroke-width: 12; }
.seg { fill: none; stroke-width: 12; stroke-linecap: butt; cursor: pointer; transition: stroke-width 0.2s; &:hover { stroke-width: 15; } }
.c-val, .c-label { transform: rotate(90deg); transform-origin: 60px 60px; text-anchor: middle; }
.c-val { font-size: 26px; font-weight: 800; fill: #142033; font-family: inherit; }
.c-label { font-size: 10px; fill: #6b7a92; }
.legend { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.lg { display: grid; grid-template-columns: 10px minmax(0, 1fr) auto 34px; white-space: nowrap; align-items: center; gap: 8px; font-size: 13px; color: #3b4a63; cursor: default; transition: opacity 0.2s;
  &.dim { opacity: 0.4; }
  i { width: 10px; height: 10px; border-radius: 3px; }
  span { overflow: hidden; text-overflow: ellipsis; }
  b { font-size: 17px; color: #142033; }
  small { color: #6b7a92; text-align: right; } }
@media (max-width: 1500px) {
  .donut { width: 92px; height: 92px; }
  .donut-wrap { gap: 10px; }
  .lg { gap: 6px; grid-template-columns: 10px minmax(0, 1fr) auto; small { display: none; } }
}
</style>
