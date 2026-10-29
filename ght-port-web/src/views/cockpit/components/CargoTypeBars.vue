<template>
  <div class="ctb" :class="{ dense }">
    <div class="legend">
      <span><i :style="{ background: SERIES.unload.color }" />{{ SERIES.unload.label }}</span>
      <span><i :style="{ background: SERIES.load.color }" />{{ SERIES.load.label }}</span>
      <span class="unit">单位：吨</span>
    </div>
    <div v-if="!rows.length" class="empty">暂无货物数据</div>
    <div v-for="r in rows" :key="r.typeName" class="row" @mouseenter="tip = r" @mouseleave="tip = null">
      <div class="name"><i :style="{ background: r.color || '#94a3b8' }" />{{ r.typeName }}</div>
      <div class="bars">
        <div class="bar"><span :style="{ width: pct(r.unloadTonnage), background: SERIES.unload.color }" /></div>
        <div class="bar"><span :style="{ width: pct(r.loadTonnage), background: SERIES.load.color }" /></div>
      </div>
      <div class="vals num">
        <span>{{ compact(r.unloadTonnage) }}</span>
        <span>{{ compact(r.loadTonnage) }}</span>
      </div>
      <div v-if="tip === r" class="tip">
        <b>{{ r.typeName }}</b>
        <div><i :style="{ background: SERIES.unload.color }" />卸货 {{ fmtNum(r.unloadTonnage, 1) }} 吨</div>
        <div><i :style="{ background: SERIES.load.color }" />装货 {{ fmtNum(r.loadTonnage, 1) }} 吨</div>
        <div class="muted">作业批次 {{ r.count }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { SERIES, compact } from './palette'
import { fmtNum } from '@/utils/format'

/** dense：紧凑模式（行距更小，用于泊位轮播） */
const props = defineProps({ data: { type: Array, default: () => [] }, limit: { type: Number, default: 6 }, dense: Boolean })
const tip = ref(null)
const rows = computed(() => props.data.slice(0, props.limit))
const max = computed(() => Math.max(1, ...rows.value.flatMap((r) => [Number(r.unloadTonnage) || 0, Number(r.loadTonnage) || 0])))
const pct = (v) => `${Math.max((Number(v) || 0) > 0 ? 2 : 0, ((Number(v) || 0) / max.value) * 100)}%`
</script>

<style scoped lang="scss">
.ctb { display: flex; flex-direction: column; gap: 9px; }
.legend { display: flex; gap: 14px; font-size: 12px; color: #3b4a63; margin-bottom: 2px;
  i { display: inline-block; width: 10px; height: 4px; border-radius: 2px; margin-right: 5px; vertical-align: middle; }
  .unit { margin-left: auto; color: #6b7a92; } }
.dense { gap: 3px; .legend { font-size: 11px; margin: 0; } .row { padding: 1px 0; } .empty { padding: 4px 0; } }
.empty { font-size: 13px; color: #6b7a92; padding: 16px 0; text-align: center; }
.row { position: relative; display: grid; grid-template-columns: 84px 1fr 54px; align-items: center; gap: 10px; padding: 3px 0; cursor: default; }
.name { font-size: 13px; color: #142033; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: flex; align-items: center; gap: 6px;
  i { width: 8px; height: 8px; border-radius: 2px; flex-shrink: 0; } }
.bars { display: flex; flex-direction: column; gap: 2px; }
.bar { height: 6px; border-radius: 4px; background: rgba(47, 91, 211, 0.07); overflow: hidden;
  span { display: block; height: 100%; border-radius: 0 4px 4px 0; transition: width 0.8s cubic-bezier(0.2, 0.8, 0.2, 1); } }
.vals { display: flex; flex-direction: column; font-size: 11px; line-height: 1.15; color: #3b4a63; text-align: right; }
.tip { position: absolute; z-index: 5; right: 0; bottom: calc(100% + 4px); padding: 8px 10px; border-radius: 10px; font-size: 12px; color: #142033; white-space: nowrap;
  background: rgba(255, 255, 255, 0.92); backdrop-filter: blur(10px); box-shadow: 0 10px 24px -10px rgba(15, 35, 102, 0.4); border: 1px solid rgba(255, 255, 255, 0.9);
  i { display: inline-block; width: 8px; height: 8px; border-radius: 2px; margin-right: 6px; }
  .muted { color: #6b7a92; margin-top: 2px; } }
</style>
