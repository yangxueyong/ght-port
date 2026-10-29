<template>
  <div class="prop-panel">
    <template v-if="el">
      <div class="head">
        <div class="type-badge">{{ def.name }}</div>
        <StatusTag v-if="berth" :dict="BERTH_STATUS" :value="berth.status" :pulse="berth.status === 'WORKING'" />
      </div>

      <div class="sec-title">位置与尺寸</div>
      <div class="grid2">
        <label>X<el-input-number v-model="el.posX" :step="10" size="small" controls-position="right" @change="emit('change')" /></label>
        <label>Y<el-input-number v-model="el.posY" :step="10" size="small" controls-position="right" @change="emit('change')" /></label>
        <label>宽<el-input-number v-model="el.width" :min="10" :step="10" size="small" controls-position="right" @change="emit('change')" /></label>
        <label>高<el-input-number v-model="el.height" :min="10" :step="10" size="small" controls-position="right" @change="emit('change')" /></label>
      </div>
      <label class="row">旋转
        <el-slider v-model="el.rotation" :min="-180" :max="180" :step="5" size="small" @change="emit('change')" />
      </label>

      <template v-if="el.elementType === 'BERTH'">
        <div class="sec-title">临水方向</div>
        <div class="dir-row">
          <el-button v-for="d in DIRS" :key="d.label" size="small" :type="el.rotation === d.rot ? 'primary' : ''" @click="setRotation(d.rot)">{{ d.label }}</el-button>
        </div>
        <el-button size="small" class="full" icon="Aim" @click="emit('dock')">自动靠岸（朝向最近河道）</el-button>
        <div class="tip-line">河道两岸均可设置泊位：把泊位拖到任意一侧岸边，点击“自动靠岸”即可贴岸并朝向水面。</div>
        <div class="sec-title">泊位信息</div>
        <label class="row">泊位编号<el-input v-model="el.berthCode" size="small" placeholder="如 B7" @change="emit('change')" /></label>
        <label class="row">泊位名称<el-input v-model="el.berthName" size="small" placeholder="如 7号泊位" @change="emit('change')" /></label>
        <template v-if="berth">
          <div class="sec-title">作业时间</div>
          <el-time-picker v-model="workRange" is-range size="small" format="HH:mm" value-format="HH:mm:ss" range-separator="至"
            start-placeholder="开始" end-placeholder="结束" style="width: 100%" />
          <div class="grid2 mt-8">
            <label>长度(米)<el-input-number v-model="berthForm.berthLength" :min="0" size="small" controls-position="right" /></label>
            <label>水深(米)<el-input-number v-model="berthForm.waterDepth" :min="0" :step="0.5" size="small" controls-position="right" /></label>
          </div>
          <label class="row">最大靠泊吨级<el-input-number v-model="berthForm.maxTonnage" :min="0" :step="500" size="small" controls-position="right" style="width: 100%" /></label>
          <el-button type="primary" plain size="small" class="full" :loading="saving" @click="saveWorkTime">保存作业设置</el-button>
        </template>
        <el-alert v-else type="info" :closable="false" show-icon title="新泊位，保存俯视图后可设置作业时间" class="mt-8" />
      </template>

      <template v-if="hasLabel">
        <div class="sec-title">外观</div>
        <label class="row">名称/文字<el-input v-model="el.cfg[labelKey]" size="small" @change="emit('change')" /></label>
      </template>
      <template v-if="el.elementType === 'RIVER'">
        <label class="row">河道宽度（{{ riverWidth }}px）
          <el-slider :model-value="riverWidth" :min="30" :max="600" :step="5" size="small" @input="setRiverWidth" @change="emit('change')" />
        </label>
        <label class="row">弯曲控制点
          <div class="pt-line">
            <span class="num">{{ (el.cfg.points || []).length || 4 }} 个</span>
            <el-button size="small" text type="primary" @click="resetRiver">恢复平直</el-button>
          </div>
        </label>
        <div class="tip-line">在场景中拖动绿色圆点调整弯度，双击河面新增控制点，双击圆点删除。</div>
      </template>
      <template v-if="el.elementType === 'MOUNTAIN'">
        <div class="sec-title">山体</div>
        <label class="row">高度（{{ el.cfg.peak || 140 }}）
          <el-slider v-model="el.cfg.peak" :min="10" :max="800" :step="5" size="small" @change="emit('change')" />
        </label>
        <label class="row">山顶积雪
          <el-radio-group :model-value="snowValue" size="small" @change="setSnow">
            <el-radio-button value="auto">自动</el-radio-button>
            <el-radio-button value="on">有</el-radio-button>
            <el-radio-button value="off">无</el-radio-button>
          </el-radio-group>
        </label>
        <el-button size="small" class="full" icon="MagicStick" @click="reseed">换一种地貌</el-button>
        <div class="tip-line">长、宽：拖动蓝色手柄或修改上方“宽/高”；高度：在场景中上下拖动山顶的绿色手柄，或拖动滑块。</div>
      </template>
      <template v-if="el.elementType === 'LAND'">
        <label class="row">地面材质
          <el-radio-group v-model="el.cfg.variant" size="small" @change="emit('change')">
            <el-radio-button value="concrete">硬化</el-radio-button>
            <el-radio-button value="grass">草地</el-radio-button>
            <el-radio-button value="sand">沙地</el-radio-button>
          </el-radio-group>
        </label>
      </template>
      <template v-if="el.elementType === 'YARD'">
        <label class="row">堆存形式
          <el-radio-group v-model="el.cfg.containers" size="small" @change="emit('change')">
            <el-radio-button :value="true">集装箱</el-radio-button>
            <el-radio-button :value="false">散货堆</el-radio-button>
          </el-radio-group>
        </label>
      </template>
      <template v-if="el.elementType === 'SHIP' || el.elementType === 'LABEL'">
        <label class="row">颜色
          <div class="swatches">
            <span v-for="c in colors" :key="c" :style="{ background: c }" :class="{ on: el.cfg.color === c }" @click="el.cfg.color = c; emit('change')" />
          </div>
        </label>
      </template>
      <template v-if="el.elementType === 'LABEL'">
        <label class="row">字号<el-slider v-model="el.cfg.fontSize" :min="10" :max="48" size="small" @change="emit('change')" /></label>
      </template>

      <div class="actions">
        <el-button size="small" icon="CopyDocument" @click="emit('duplicate')">复制</el-button>
        <el-button size="small" icon="Top" @click="emit('front')">置顶</el-button>
        <el-button size="small" icon="Bottom" @click="emit('back')">置底</el-button>
        <el-button size="small" type="danger" plain icon="Delete" @click="emit('remove')">删除元素</el-button>
      </div>
    </template>

    <template v-else>
      <div class="head"><div class="type-badge">画布</div></div>
      <div class="sec-title">画布尺寸</div>
      <div class="grid2">
        <label>宽<el-input-number v-model="canvas.width" :min="800" :max="4000" :step="100" size="small" controls-position="right" @change="emit('change')" /></label>
        <label>高<el-input-number v-model="canvas.height" :min="500" :max="3000" :step="100" size="small" controls-position="right" @change="emit('change')" /></label>
      </div>
      <div class="sec-title">主题</div>
      <el-radio-group v-model="canvas.theme" size="small" @change="emit('change')">
        <el-radio-button value="DAY">白昼</el-radio-button>
        <el-radio-button value="DUSK">黄昏</el-radio-button>
        <el-radio-button value="NIGHT">夜间</el-radio-button>
      </el-radio-group>
      <div class="sec-title">统计</div>
      <div class="stats">
        <div><b class="num">{{ elements.length }}</b><span>元素</span></div>
        <div><b class="num">{{ berthCount }}</b><span>泊位</span></div>
      </div>
      <div class="sec-title">元素清单（点击选中）</div>
      <div class="el-list">
        <div v-for="e in sortedElements" :key="e.uid" class="el-row" @click="emit('select', e.uid)">
          <span class="dot" :style="{ background: dotColor(e) }" />
          <span class="el-name">{{ displayName(e) }}</span>
          <el-icon class="del" title="删除" @click.stop="emit('remove-el', e.uid)"><Delete /></el-icon>
        </div>
      </div>
      <div class="hint">
        <p>左键拖动空白处旋转视角 · 右键平移 · 滚轮缩放</p>
        <p><kbd>Delete</kbd> 删除选中 · <kbd>R</kbd> 旋转 15°</p>
        <p><kbd>Ctrl</kbd>+<kbd>Z</kbd> 撤销 · <kbd>Ctrl</kbd>+<kbd>Y</kbd> 重做</p>
        <p><kbd>Ctrl</kbd>+<kbd>D</kbd> 复制 · 方向键微调</p>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/common/StatusTag.vue'
import { ELEMENT_TYPES } from './elementRegistry'
import { BERTH_STATUS, PALETTE_COLORS } from '@/utils/dict'
import { berthWorkTime } from '@/api/berth'
import { withConfirm } from '@/utils/confirm'
import { defaultPoints } from '@/components/port3d/engine/river'

const props = defineProps({
  el: Object,
  berth: Object,
  canvas: { type: Object, required: true },
  elements: { type: Array, default: () => [] },
  berthMap: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['change', 'duplicate', 'front', 'back', 'remove', 'berth-saved', 'select', 'remove-el', 'dock'])
/** 临水方向：泊位局部 -z 为临水侧，旋转角度对应朝向 */
const DIRS = [
  { label: '朝北', rot: 0 },
  { label: '朝南', rot: 180 },
  { label: '朝东', rot: 90 },
  { label: '朝西', rot: -90 }
]
const STATUS_DOT = { IDLE: '#22c55e', WORKING: '#f59e0b', MAINTENANCE: '#ef4444' }
const berthCount = computed(() => props.elements.filter((e) => e.elementType === 'BERTH').length)
const sortedElements = computed(() => [...props.elements].sort((a, b) => (b.layerIndex ?? 0) - (a.layerIndex ?? 0)))
function displayName(e) {
  const def = ELEMENT_TYPES[e.elementType] || {}
  if (e.elementType === 'BERTH') return `${def.name} · ${e.berthCode || ''} ${e.berthName || ''}`
  const label = e.cfg?.label || e.cfg?.text
  return label ? `${def.name} · ${label}` : def.name || e.elementType
}
function dotColor(e) {
  if (e.elementType === 'BERTH') return STATUS_DOT[props.berthMap[e.berthId]?.status] || STATUS_DOT.IDLE
  return e.elementType === 'RIVER' ? '#4a82de' : '#94a3b8'
}
const snowValue = computed(() => (props.el?.cfg?.snow === undefined ? 'auto' : props.el.cfg.snow ? 'on' : 'off'))
function setSnow(v) {
  if (v === 'auto') delete props.el.cfg.snow
  else props.el.cfg.snow = v === 'on'
  emit('change')
}
function reseed() {
  props.el.cfg.seed = +(Math.random() * 10).toFixed(2)
  emit('change')
}
function setRotation(rot) {
  props.el.rotation = rot
  emit('change')
}
const colors = PALETTE_COLORS
const def = computed(() => (props.el ? ELEMENT_TYPES[props.el.elementType] || {} : {}))
const labelKey = computed(() => (props.el?.elementType === 'LABEL' ? 'text' : 'label'))
const hasLabel = computed(() => ['RIVER', 'YARD', 'WAREHOUSE', 'GATE', 'BUILDING', 'SHIP', 'LABEL'].includes(props.el?.elementType))

const riverWidth = computed(() => Number(props.el?.cfg?.riverWidth) || Math.round(Math.min(props.el?.width || 0, props.el?.height || 0)))
function setRiverWidth(v) {
  props.el.cfg.riverWidth = v
}
function resetRiver() {
  props.el.cfg.points = defaultPoints(props.el.width, props.el.height)
  emit('change')
}

const workRange = ref(null)
const berthForm = reactive({ berthLength: null, waterDepth: null, maxTonnage: null })
const saving = ref(false)
watch(
  () => props.berth,
  (b) => {
    workRange.value = b && b.workStartTime && b.workEndTime ? [b.workStartTime, b.workEndTime] : null
    berthForm.berthLength = b?.berthLength ?? null
    berthForm.waterDepth = b?.waterDepth ?? null
    berthForm.maxTonnage = b?.maxTonnage ?? null
  },
  { immediate: true }
)

async function saveWorkTime() {
  if (!workRange.value) {
    ElMessage.warning('请选择作业时间')
    return
  }
  saving.value = true
  try {
    const res = await withConfirm(berthWorkTime, {
      id: props.berth.id,
      workStartTime: workRange.value[0],
      workEndTime: workRange.value[1],
      ...berthForm
    })
    if (res) {
      ElMessage.success('作业设置已保存')
      emit('berth-saved')
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.prop-panel { padding: 14px; font-size: 13px; }
.head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.type-badge { font-weight: 700; font-size: 15px; }
.sec-title { margin: 14px 0 8px; font-size: 12px; color: var(--ght-ink-3); letter-spacing: 1px; }
.grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; label { display: flex; flex-direction: column; gap: 4px; color: var(--ght-ink-3); font-size: 12px; } :deep(.el-input-number) { width: 100%; } }
.row { display: flex; flex-direction: column; gap: 6px; margin-top: 8px; color: var(--ght-ink-3); font-size: 12px; }
.mt-8 { margin-top: 8px; }
.full { width: 100%; margin-top: 10px; }
.actions { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-top: 18px; .el-button { margin: 0; } }
.swatches { display: flex; flex-wrap: wrap; gap: 6px; span { width: 20px; height: 20px; border-radius: 6px; cursor: pointer; border: 2px solid transparent; &.on { border-color: #fff; box-shadow: 0 0 0 2px var(--ght-blue); } } }
.stats { display: flex; gap: 10px; div { flex: 1; background: #f6f8fc; border-radius: 10px; padding: 10px; display: flex; flex-direction: column; } b { font-size: 22px; } span { color: var(--ght-ink-3); font-size: 12px; } }
.pt-line { display: flex; align-items: center; justify-content: space-between; color: var(--ght-ink); }
.tip-line { font-size: 12px; color: var(--ght-ink-3); margin-top: 8px; line-height: 1.6; }
.dir-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 4px; .el-button { margin: 0; padding: 5px 0; } }
.el-list { max-height: 260px; overflow: auto; border: 1px solid var(--ght-line); border-radius: 10px; background: #fff; }
.el-row { display: flex; align-items: center; gap: 8px; padding: 6px 10px; cursor: pointer; font-size: 12px; border-bottom: 1px solid #f1f4f9;
  &:last-child { border-bottom: 0; } &:hover { background: #f3f7ff; .del { opacity: 1; } } }
.dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.el-name { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: var(--ght-ink-2); }
.del { color: var(--ght-maint); opacity: 0.45; transition: opacity 0.2s; }
.hint { margin-top: 18px; color: var(--ght-ink-3); font-size: 12px; line-height: 1.9; p { margin: 0; } }
kbd { background: #eef2f8; border: 1px solid #d8e0ec; border-bottom-width: 2px; border-radius: 4px; padding: 0 4px; font-size: 11px; font-family: inherit; }
</style>
