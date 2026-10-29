<template>
  <div class="cargo-card" :class="[cargo.direction, { open }]">
    <div class="head" @click="emit('toggle')">
      <div class="flex-between">
        <div class="cargo-name">
          <span class="dir" :style="{ color: dir.color, background: dir.bg }">{{ dir.label }}</span>
          {{ cargo.cargoName }}
          <span class="type-chip">{{ cargo.cargoTypeName }}</span>
          <span class="pack-chip">{{ labelOf(PACK_TYPES, cargo.packType) }}</span>
        </div>
        <span class="muted small">{{ cargo.direction === 'LOAD' ? '登记' : '到货' }} {{ fmtTime(cargo.arriveTime, 'MM-DD HH:mm') }}</span>
      </div>
      <div class="progress-grid">
        <div v-for="m in metrics" :key="m.label" class="pg">
          <div class="flex-between small"><span class="muted">{{ m.label }}</span><span class="num"><b>{{ fmtNum(m.loaded, 3) }}</b> / {{ fmtNum(m.total, 3) }} {{ m.unit }}</span></div>
          <el-progress :percentage="percent(m.loaded, m.total)" :stroke-width="8" :show-text="false" :color="m.color" />
        </div>
      </div>
      <div class="foot">
        <div class="ships">
          <div v-for="s in cargo.ships" :key="s.id" class="ship-chip">
            <ShipSvg :type="s.shipType" :length="s.shipLength" :color="s.hullColor || '#1f6fd1'" :width="64" />
            <div>
              <div class="small"><b>{{ s.shipName }}</b></div>
              <div class="muted small">{{ s.companyName }}</div>
            </div>
          </div>
        </div>
        <div class="foot-right">
        <el-button v-if="assignable" size="small" :type="cargo.direction === 'LOAD' ? 'warning' : 'primary'" :icon="cargo.direction === 'LOAD' ? 'Ship' : 'Van'" @click.stop="emit('assign')">
          {{ cargo.direction === 'LOAD' ? '分派船只' : '分派承运' }}
        </el-button>
        <span v-else-if="done && cargo.status === 'IN_PROGRESS'" class="done-tag"><el-icon><CircleCheck /></el-icon>{{ cargo.direction === 'LOAD' ? '已全部装船' : '已全部运出' }}</span>
        <div class="more" :class="{ has: cargo.assignCount }">
          <template v-if="cargo.assignCount">{{ cargo.direction === 'LOAD' ? `已分派 ${cargo.assignCount} 艘船` : `已分派 ${cargo.assignCount} 车次` }}</template>
          <template v-else>{{ cargo.direction === 'LOAD' ? '未分派船只' : '未分派车辆' }}</template>
          <el-icon class="arrow"><ArrowDown /></el-icon>
        </div>
        </div>
      </div>
    </div>
    <el-collapse-transition>
      <div v-if="open" class="body">
        <div class="body-title">{{ cargo.direction === 'LOAD' ? '装船船只' : '承运商与车辆' }}</div>
        <CargoAssignPanel :cargo-id="cargo.id" :stamp="stamp" />
      </div>
    </el-collapse-transition>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import ShipSvg from '@/components/svg/ShipSvg.vue'
import CargoAssignPanel from './CargoAssignPanel.vue'
import { CARGO_DIRECTION, PACK_TYPES, labelOf } from '@/utils/dict'
import { fmtNum, fmtTime, percent } from '@/utils/format'

/** assignable：显示“分派承运 / 分派船只”按钮（仅当前作业的货物） */
const props = defineProps({ cargo: { type: Object, required: true }, open: Boolean, stamp: Number, assignable: Boolean })
const emit = defineEmits(['toggle', 'assign'])
/** 已全部运出/装船：有登记的吨数、方数、件数剩余都为 0 */
const done = computed(() => {
  const c = props.cargo
  const pairs = [[c.tonnage, c.loadedTonnage], [c.volume, c.loadedVolume], [c.pieces, c.loadedPieces]].filter(([t]) => Number(t) > 0)
  return pairs.length > 0 && pairs.every(([t, l]) => Number(l) >= Number(t))
})
const dir = computed(() => CARGO_DIRECTION[props.cargo.direction] || CARGO_DIRECTION.UNLOAD)
const metrics = computed(() => {
  const c = props.cargo
  const verb = c.direction === 'LOAD' ? '已装船' : '已装车'
  return [
    { label: `吨位（${verb}）`, loaded: c.loadedTonnage, total: c.tonnage, unit: '吨', color: c.direction === 'LOAD' ? '#7c3aed' : '#1f7ae0' },
    { label: '方数', loaded: c.loadedVolume, total: c.volume, unit: '方', color: '#22c3e6' },
    { label: c.packType === 'CONTAINER' ? '箱数' : '件数', loaded: c.loadedPieces, total: c.pieces, unit: c.packType === 'CONTAINER' ? '箱' : '件', color: '#f59f00' }
  ].filter((m, i) => i === 0 || Number(m.total) > 0)
})
</script>

<style scoped lang="scss">
.cargo-card { border: 1px solid var(--ght-line); border-radius: 14px; margin-bottom: 12px; overflow: hidden; transition: all 0.2s;
  &:hover { border-color: #bcd5f5; }
  &.open { border-color: var(--ght-blue); box-shadow: 0 10px 24px -18px rgba(31, 122, 224, 0.8); .arrow { transform: rotate(180deg); } }
  &.LOAD.open { border-color: #a78bfa; } }
.head { padding: 14px; cursor: pointer; }
.cargo-name { font-weight: 700; font-size: 15px; display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.dir { font-size: 11px; padding: 1px 7px; border-radius: 6px; font-weight: 600; }
.type-chip { font-size: 11px; font-weight: 500; padding: 2px 8px; border-radius: 6px; background: #e8f1fd; color: #1f7ae0; }
.pack-chip { font-size: 11px; font-weight: 500; padding: 2px 8px; border-radius: 6px; background: #f1f3f7; color: var(--ght-ink-2); }
.small { font-size: 12px; }
.progress-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 12px; }
.pg { display: flex; flex-direction: column; gap: 6px; }
.foot { display: flex; align-items: flex-end; justify-content: space-between; gap: 10px; margin-top: 12px; }
.ships { display: flex; flex-wrap: wrap; gap: 10px; }
.ship-chip { display: flex; align-items: center; gap: 8px; padding: 6px 10px; border-radius: 12px; background: #f3f8ff; }
.done-tag { display: inline-flex; align-items: center; gap: 4px; font-size: 12px; font-weight: 600; color: #12b886; padding: 3px 10px; border-radius: 999px; background: #e6f8f1; }
.foot-right { flex-shrink: 0; display: flex; flex-direction: column; align-items: flex-end; gap: 8px; }
.more { flex-shrink: 0; display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--ght-ink-3); white-space: nowrap;
  &.has { color: var(--ght-blue); font-weight: 600; } .arrow { transition: transform 0.2s; } }
.body { padding: 12px 14px 14px; border-top: 1px dashed var(--ght-line); background: #fcfdff; }
.body-title { font-size: 12px; color: var(--ght-ink-3); margin-bottom: 8px; letter-spacing: 1px; }
</style>
