<template>
  <div class="editor" :class="{ readonly, 'is-full': fullscreen }">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="tool-group">
        <slot name="title"><span class="port-title">{{ port?.portName }}</span></slot>
        <el-tag v-if="dirty" type="warning" size="small" effect="light" round>未保存</el-tag>
      </div>
      <div v-if="!readonly" class="tool-group">
        <el-tooltip content="撤销 Ctrl+Z"><el-button text :disabled="!canUndo" icon="RefreshLeft" @click="undo" /></el-tooltip>
        <el-tooltip content="重做 Ctrl+Y"><el-button text :disabled="!canRedo" icon="RefreshRight" @click="redo" /></el-tooltip>
        <el-divider direction="vertical" />
        <el-tooltip content="复制 Ctrl+D"><el-button text :disabled="!selected" icon="CopyDocument" @click="duplicate" /></el-tooltip>
        <el-tooltip content="删除 Delete"><el-button text :disabled="!selected" icon="Delete" @click="removeSelected" /></el-tooltip>
        <el-divider direction="vertical" />
        <el-tooltip content="吸附网格"><el-button text :type="snap ? 'primary' : ''" icon="Grid" @click="snap = !snap" /></el-tooltip>
      </div>
      <div class="tool-group">
        <el-button-group class="views">
          <el-button size="small" @click="setView('iso')">斜视</el-button>
          <el-button size="small" @click="setView('top')">俯视</el-button>
          <el-button size="small" @click="setView('front')">平视</el-button>
        </el-button-group>
        <el-divider direction="vertical" />
        <el-button text icon="ZoomOut" @click="zoomBy(1.2)" />
        <span class="zoom num">{{ Math.round(zoomPct) }}%</span>
        <el-button text icon="ZoomIn" @click="zoomBy(1 / 1.2)" />
        <el-button text icon="Aim" @click="setView('iso')">适应</el-button>
        <el-tooltip :content="fullscreen ? '退出全屏 Esc' : '全屏编辑'">
          <el-button text :icon="fullscreen ? 'Close' : 'FullScreen'" @click="toggleFullscreen">{{ fullscreen ? '退出全屏' : '全屏' }}</el-button>
        </el-tooltip>
        <template v-if="!readonly">
          <el-divider direction="vertical" />
          <el-button :type="preview ? 'primary' : ''" plain icon="View" @click="preview = !preview">{{ preview ? '退出预览' : '预览效果' }}</el-button>
          <el-button type="primary" icon="Check" :loading="saving" @click="save">保存港口</el-button>
        </template>
      </div>
    </div>

    <div class="body">
      <aside v-if="!readonly && !preview" class="left">
        <ElementPalette @add="addAtCenter" />
      </aside>

      <div ref="stageRef" class="stage" @dragover.prevent @drop="onDrop">
        <PortCanvas3D ref="canvasRef" :elements="elements" :berth-map="berthMap" :width="canvas.width" :height="canvas.height"
          :theme="canvas.theme" :mode="readonly || preview ? 'berth' : 'edit'" :selected-uid="selectedUid"
          @el-pointerdown="onElDown" @handle-pointerdown="onHandleDown" @handle-dblclick="onHandleDblclick" @bg-click="selectedUid = null"
          @el-dblclick="onElDblclick" @view-change="syncZoom" />
        <div class="legend">
          <span><i style="background: var(--ght-idle)" />未作业</span>
          <span><i style="background: var(--ght-working)" />正在作业</span>
          <span><i style="background: var(--ght-maint)" />维修中</span>
        </div>
        <div v-if="selected && selected.elementType === 'MOUNTAIN' && !readonly && !preview" class="river-tip">
          <el-icon><Pointer /></el-icon> 拖动蓝点调整山体长宽 · 上下拖动绿点调整高度 · 橙点旋转
        </div>
        <div v-if="selected && selected.elementType === 'RIVER' && !readonly && !preview" class="river-tip">
          <el-icon><Pointer /></el-icon> 拖动绿色圆点调整河道弯度 · 双击河面添加控制点 · 双击圆点删除
        </div>
        <div class="op-tip">左键拖动空白处旋转视角 · 右键平移 · 滚轮缩放<template v-if="!readonly && !preview"> · 拖动元素移动 · 蓝点缩放 · 橙点旋转</template></div>
        <div v-if="!elements.length" class="empty-tip">
          <el-icon><Pointer /></el-icon>
          <p>从左侧拖入“江河”“陆地”“泊位”等元素，开始搭建 3D 港口</p>
        </div>
      </div>

      <aside v-if="!readonly && !preview" class="right">
        <PropertyPanel :el="selected" :berth="selected && selected.berthId ? berthMap[selected.berthId] : null" :canvas="canvas"
          :elements="elements" :berth-map="berthMap"
          @change="commit" @duplicate="duplicate" @front="toFront" @back="toBack" @remove="removeSelected" @berth-saved="emit('reload')"
          @select="(uid) => (selectedUid = uid)" @remove-el="removeByUid" @dock="dockSelected" />
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PortCanvas3D from '@/components/port3d/PortCanvas3D.vue'
import ElementPalette from '@/components/port/ElementPalette.vue'
import PropertyPanel from '@/components/port/PropertyPanel.vue'
import { ELEMENT_TYPES, toModel, toPayload } from '@/components/port/elementRegistry'
import { defaultPoints } from '@/components/port3d/engine/river'
import { dockToRiver } from '@/components/port3d/engine/dock'
import { segDist2 } from '@/utils/curve'
import { portSaveMap } from '@/api/port'
import { withConfirm } from '@/utils/confirm'

const props = defineProps({
  detail: { type: Object, default: null },
  readonly: Boolean
})
const emit = defineEmits(['saved', 'reload'])

const canvasRef = ref()
const stageRef = ref()
const elements = ref([])
const selectedUid = ref(null)
const canvas = reactive({ width: 1600, height: 900, theme: 'DAY' })
const snap = ref(true)
const preview = ref(false)
const saving = ref(false)
const dirty = ref(false)
const zoomPct = ref(100)
const history = ref([])
const cursor = ref(-1)
const confirmedWorking = ref(false)
const fullscreen = ref(false)

const port = computed(() => props.detail?.port)
const berthMap = computed(() => Object.fromEntries((props.detail?.berths || []).map((b) => [b.id, b])))
const selected = computed(() => elements.value.find((e) => e.uid === selectedUid.value) || null)
const canUndo = computed(() => cursor.value > 0)
const canRedo = computed(() => cursor.value < history.value.length - 1)
const GRID = 10
const MIN_SIZE = 16
const MIN_PEAK = 10
const MAX_PEAK = 800

/** 屏幕坐标 -> 画布坐标（3D 场景投影） */
const toCanvas = (evt) => canvasRef.value?.toCanvasPoint(evt.clientX, evt.clientY)

/* ---------------- 江河控制点 ---------------- */
function ensureRiver(el) {
  if (!el.cfg.points || el.cfg.points.length < 2) el.cfg.points = defaultPoints(el.width, el.height)
  if (!el.cfg.riverWidth) el.cfg.riverWidth = Math.round(Math.min(el.width, el.height))
}
/** 画布坐标 -> 元素局部坐标（考虑旋转，原点为元素左上角） */
function toLocal(el, cx, cy) {
  const x = cx - el.posX - el.width / 2
  const y = cy - el.posY - el.height / 2
  const r = (-(el.rotation || 0) * Math.PI) / 180
  const lx = x * Math.cos(r) - y * Math.sin(r)
  const ly = x * Math.sin(r) + y * Math.cos(r)
  return { x: lx + el.width / 2, y: ly + el.height / 2 }
}
function onPointDown(evt, idx) {
  const el = selected.value
  if (!el) return
  ensureRiver(el)
  drag = { kind: 'point', el, idx, moved: false }
  listen()
}
function onElDblclick(evt, el) {
  if (props.readonly || preview.value || el.elementType !== 'RIVER') return
  selectedUid.value = el.uid
  ensureRiver(el)
  const c = toCanvas(evt)
  if (!c) return
  const l = toLocal(el, c.x, c.y)
  const abs = el.cfg.points.map((p) => ({ x: p.u * el.width, y: p.v * el.height }))
  let best = 0
  let bestD = Infinity
  for (let i = 0; i < abs.length - 1; i++) {
    const dd = segDist2(l.x, l.y, abs[i], abs[i + 1])
    if (dd < bestD) {
      bestD = dd
      best = i
    }
  }
  el.cfg.points.splice(best + 1, 0, { u: +(l.x / el.width).toFixed(4), v: +(l.y / el.height).toFixed(4) })
  pushHistory()
}
function removePoint(idx) {
  const el = selected.value
  if (!el) return
  ensureRiver(el)
  if (el.cfg.points.length <= 2) {
    ElMessage.info('河道至少需要 2 个控制点')
    return
  }
  el.cfg.points.splice(idx, 1)
  fitRiverBox(el)
  pushHistory()
}
/** 控制点拖出元素框后，按控制点与河宽重新计算元素框，保持河道外观不变 */
function fitRiverBox(el) {
  if (el.rotation) return
  const half = (Number(el.cfg.riverWidth) || 0) / 2
  const abs = el.cfg.points.map((p) => ({ x: el.posX + p.u * el.width, y: el.posY + p.v * el.height }))
  const xs = abs.map((p) => p.x)
  const ys = abs.map((p) => p.y)
  const horizontal = Math.max(...xs) - Math.min(...xs) >= Math.max(...ys) - Math.min(...ys)
  const minX = Math.round(Math.min(...xs) - (horizontal ? 0 : half))
  const maxX = Math.round(Math.max(...xs) + (horizontal ? 0 : half))
  const minY = Math.round(Math.min(...ys) - (horizontal ? half : 0))
  const maxY = Math.round(Math.max(...ys) + (horizontal ? half : 0))
  const w = Math.max(20, maxX - minX)
  const h = Math.max(20, maxY - minY)
  el.cfg.points = abs.map((p) => ({ u: +((p.x - minX) / w).toFixed(4), v: +((p.y - minY) / h).toFixed(4) }))
  el.posX = minX
  el.posY = minY
  el.width = w
  el.height = h
}

watch(
  () => props.detail,
  (d) => {
    if (!d) return
    canvas.width = d.port.canvasWidth || 1600
    canvas.height = d.port.canvasHeight || 900
    canvas.theme = d.port.theme || 'DAY'
    elements.value = (d.elements || []).map(toModel)
    selectedUid.value = null
    history.value = []
    cursor.value = -1
    confirmedWorking.value = false
    pushHistory()
    dirty.value = false
    nextTick(() => setView('iso'))
  },
  { immediate: true }
)

/* ---------------- 历史记录 ---------------- */
function snapshot() {
  return JSON.stringify({ els: elements.value, canvas: { ...canvas } })
}
function pushHistory() {
  const snap = snapshot()
  if (history.value[cursor.value] === snap) return
  history.value = history.value.slice(0, cursor.value + 1)
  history.value.push(snap)
  if (history.value.length > 60) history.value.shift()
  cursor.value = history.value.length - 1
  dirty.value = true
}
function restore(idx) {
  const s = JSON.parse(history.value[idx])
  elements.value = s.els
  Object.assign(canvas, s.canvas)
  cursor.value = idx
  dirty.value = true
  if (!elements.value.find((e) => e.uid === selectedUid.value)) selectedUid.value = null
}
const undo = () => canUndo.value && restore(cursor.value - 1)
const redo = () => canRedo.value && restore(cursor.value + 1)
const commit = () => pushHistory()

/* ---------------- 正在作业泊位提示 ---------------- */
function isWorkingBerth(el) {
  return el && el.elementType === 'BERTH' && el.berthId && berthMap.value[el.berthId]?.status === 'WORKING'
}
async function confirmWorking(el, action) {
  if (!isWorkingBerth(el)) return true
  try {
    await ElMessageBox.confirm(`泊位【${berthMap.value[el.berthId].berthName}】正在作业，是否继续${action}？`, '正在作业', {
      type: 'warning',
      confirmButtonText: '继续操作',
      cancelButtonText: '取消'
    })
    confirmedWorking.value = true
    return true
  } catch (e) {
    return false
  }
}

/* ---------------- 添加元素 ---------------- */
function nextBerthCode() {
  const used = new Set(elements.value.filter((e) => e.elementType === 'BERTH').map((e) => e.berthCode || berthMap.value[e.berthId]?.berthCode))
  let i = 1
  while (used.has('B' + i)) i++
  return 'B' + i
}
function createElement(type, cx, cy) {
  const def = ELEMENT_TYPES[type]
  const el = {
    uid: 'n' + Date.now() + Math.random().toString(16).slice(2, 6),
    elementType: type,
    posX: snapV(cx - def.w / 2),
    posY: snapV(cy - def.h / 2),
    width: def.w,
    height: def.h,
    rotation: 0,
    layerIndex: maxLayer() + 1,
    cfg: JSON.parse(JSON.stringify(def.cfg)),
    berthId: null,
    berthCode: '',
    berthName: ''
  }
  if (type === 'BERTH') {
    el.berthCode = nextBerthCode()
    el.berthName = el.berthCode.replace('B', '') + '号泊位'
    const dock = dockToRiver(el, elements.value, canvas.width, canvas.height)
    if (dock) Object.assign(el, dock)
  }
  if (type === 'LAND' || type === 'RIVER') el.layerIndex = minLayer() - 1
  elements.value.push(el)
  selectedUid.value = el.uid
  pushHistory()
}
function onDrop(e) {
  if (props.readonly || preview.value) return
  const type = e.dataTransfer.getData('text/ght-element')
  if (!type || !ELEMENT_TYPES[type]) return
  const pt = toCanvas(e) || canvasRef.value.centerPoint()
  createElement(type, pt.x, pt.y)
}
function addAtCenter(type) {
  const c = canvasRef.value?.centerPoint() || { x: canvas.width / 2, y: canvas.height / 2 }
  createElement(type, c.x, c.y)
}
const maxLayer = () => elements.value.reduce((m, e) => Math.max(m, e.layerIndex ?? 0), 0)
const minLayer = () => elements.value.reduce((m, e) => Math.min(m, e.layerIndex ?? 0), 0)
const snapV = (v) => (snap.value ? Math.round(v / GRID) * GRID : Math.round(v))

/* ---------------- 拖拽移动 / 缩放 / 旋转 ---------------- */
let drag = null
function listen() {
  window.addEventListener('pointermove', onMove)
  window.addEventListener('pointerup', onUp)
}
function onElDown(evt, el) {
  selectedUid.value = el.uid
  const pt = toCanvas(evt)
  if (!pt) return
  drag = { kind: 'move', el, sx: pt.x, sy: pt.y, ox: el.posX, oy: el.posY, moved: false }
  listen()
}
function onHandleDown(evt, kind, idx) {
  const el = selected.value
  if (!el) return
  if (kind === 'point') return onPointDown(evt, idx)
  if (kind === 'rotate') {
    drag = { kind: 'rotate', el, moved: false }
    listen()
    return
  }
  if (kind === 'height') {
    // 山体高度：按屏幕竖直位移换算，向上拖动增高
    drag = { kind: 'height', el, sy: evt.clientY, oh: Number(el.cfg.peak) || 140, k: canvasRef.value?.worldPerPixel() || 1, moved: false }
    listen()
    return
  }
  const pt = toCanvas(evt)
  if (!pt) return
  drag = { kind: 'resize', el, sx: pt.x, sy: pt.y, ow: el.width, oh: el.height, ox: el.posX, oy: el.posY, moved: false }
  listen()
}
function onHandleDblclick(evt, kind, idx) {
  if (kind === 'point') removePoint(idx)
}
function onMove(evt) {
  if (!drag) return
  if (drag.kind === 'height') {
    const dy = drag.sy - evt.clientY
    if (Math.abs(dy) > 1) drag.moved = true
    drag.el.cfg.peak = Math.round(Math.min(MAX_PEAK, Math.max(MIN_PEAK, drag.oh + dy * drag.k * 1.4)))
    return
  }
  const pt = toCanvas(evt)
  if (!pt) return
  const el = drag.el
  if (drag.kind === 'point') {
    const l = toLocal(el, pt.x, pt.y)
    const p = el.cfg.points[drag.idx]
    p.u = +(snapV(l.x) / el.width).toFixed(4)
    p.v = +(snapV(l.y) / el.height).toFixed(4)
    drag.moved = true
    return
  }
  if (drag.kind === 'rotate') {
    const cx = el.posX + el.width / 2
    const cy = el.posY + el.height / 2
    // 旋转手柄位于局部 +y（下方）一侧
    let deg = (Math.atan2(pt.y - cy, pt.x - cx) * 180) / Math.PI - 90
    if (deg <= -180) deg += 360
    el.rotation = Math.round(deg / 5) * 5
    drag.moved = true
    return
  }
  let dx = pt.x - drag.sx
  let dy = pt.y - drag.sy
  if (Math.abs(dx) + Math.abs(dy) > 1) drag.moved = true
  if (drag.kind === 'move') {
    el.posX = snapV(drag.ox + dx)
    el.posY = snapV(drag.oy + dy)
    return
  }
  // 缩放：以对角为锚点，在元素局部坐标中计算
  const r = (-(el.rotation || 0) * Math.PI) / 180
  const ldx = dx * Math.cos(r) - dy * Math.sin(r)
  const ldy = dx * Math.sin(r) + dy * Math.cos(r)
  const w = Math.max(MIN_SIZE, snapV(drag.ow + ldx))
  const h = Math.max(MIN_SIZE, snapV(drag.oh + ldy))
  const cx0 = drag.ox + drag.ow / 2
  const cy0 = drag.oy + drag.oh / 2
  const ax = -drag.ow / 2
  const ay = -drag.oh / 2
  const a = (-r)
  const anchorX = cx0 + ax * Math.cos(a) - ay * Math.sin(a)
  const anchorY = cy0 + ax * Math.sin(a) + ay * Math.cos(a)
  const ncx = anchorX + (w / 2) * Math.cos(a) - (h / 2) * Math.sin(a)
  const ncy = anchorY + (w / 2) * Math.sin(a) + (h / 2) * Math.cos(a)
  el.width = w
  el.height = h
  el.posX = Math.round(ncx - w / 2)
  el.posY = Math.round(ncy - h / 2)
}
async function onUp() {
  window.removeEventListener('pointermove', onMove)
  window.removeEventListener('pointerup', onUp)
  const d = drag
  drag = null
  if (!d || !d.moved) return
  if (d.kind === 'point') {
    fitRiverBox(d.el)
    pushHistory()
    return
  }
  const ok = await confirmWorking(d.el, d.kind === 'move' ? '调整位置' : d.kind === 'rotate' ? '旋转' : d.kind === 'height' ? '调整高度' : '调整大小')
  if (!ok) {
    restore(cursor.value)
    return
  }
  pushHistory()
}

/* ---------------- 选中元素操作 ---------------- */
async function removeByUid(uid) {
  const el = elements.value.find((e) => e.uid === uid)
  if (!el) return
  if (!(await confirmWorking(el, '移除'))) return
  elements.value = elements.value.filter((e) => e.uid !== uid)
  if (selectedUid.value === uid) selectedUid.value = null
  pushHistory()
}
const removeSelected = () => selected.value && removeByUid(selected.value.uid)
async function dockSelected() {
  const el = selected.value
  if (!el) return
  const dock = dockToRiver(el, elements.value, canvas.width, canvas.height)
  if (!dock) {
    ElMessage.info('附近没有可停靠的河道，请先将泊位拖到河岸边')
    return
  }
  if (!(await confirmWorking(el, '调整位置'))) return
  Object.assign(el, dock)
  pushHistory()
}
function duplicate() {
  const el = selected.value
  if (!el) return
  const copy = JSON.parse(JSON.stringify(el))
  copy.uid = 'n' + Date.now() + Math.random().toString(16).slice(2, 6)
  copy.posX += 20
  copy.posY += 20
  copy.layerIndex = maxLayer() + 1
  if (copy.elementType === 'BERTH') {
    copy.berthId = null
    copy.berthCode = nextBerthCode()
    copy.berthName = copy.berthCode.replace('B', '') + '号泊位'
    copy.posX += el.width
    copy.posY -= 20
  }
  elements.value.push(copy)
  selectedUid.value = copy.uid
  pushHistory()
}
function toFront() {
  if (!selected.value) return
  selected.value.layerIndex = maxLayer() + 1
  pushHistory()
}
function toBack() {
  if (!selected.value) return
  selected.value.layerIndex = minLayer() - 1
  pushHistory()
}

/* ---------------- 视角 ---------------- */
function setView(v) {
  canvasRef.value?.fit(v)
  syncZoom()
}
function zoomBy(f) {
  canvasRef.value?.zoomBy(f)
  syncZoom()
}
function syncZoom() {
  zoomPct.value = canvasRef.value?.zoomPct() || 100
}

/* ---------------- 键盘 ---------------- */
/* ---------------- 全屏编辑 ---------------- */
/** 编辑器铺满窗口；同时请求浏览器全屏（对 documentElement，保证弹窗/下拉仍可见） */
async function toggleFullscreen() {
  fullscreen.value = !fullscreen.value
  try {
    if (fullscreen.value && !document.fullscreenElement) await document.documentElement.requestFullscreen?.()
    else if (!fullscreen.value && document.fullscreenElement) await document.exitFullscreen?.()
  } catch (e) {
    // 浏览器不允许全屏时仅铺满窗口
  }
}
function onFullscreenChange() {
  if (!document.fullscreenElement && fullscreen.value) fullscreen.value = false
}
onMounted(() => document.addEventListener('fullscreenchange', onFullscreenChange))
onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  if (fullscreen.value && document.fullscreenElement) document.exitFullscreen?.()
})

function onKey(e) {
  if (e.key === 'Escape' && fullscreen.value && !document.fullscreenElement) { fullscreen.value = false; return }
  if (props.readonly || preview.value) return
  const tag = (e.target.tagName || '').toLowerCase()
  if (['input', 'textarea'].includes(tag)) return
  const ctrl = e.ctrlKey || e.metaKey
  if (ctrl && e.key.toLowerCase() === 'z') { e.preventDefault(); e.shiftKey ? redo() : undo(); return }
  if (ctrl && e.key.toLowerCase() === 'y') { e.preventDefault(); redo(); return }
  if (ctrl && e.key.toLowerCase() === 'd') { e.preventDefault(); duplicate(); return }
  if (!selected.value) return
  if (e.key === 'Delete' || e.key === 'Backspace') { e.preventDefault(); removeSelected(); return }
  if (e.key.toLowerCase() === 'r') {
    selected.value.rotation = (((selected.value.rotation || 0) + (e.shiftKey ? -15 : 15) + 540) % 360) - 180
    clearTimeout(nudgeTimer)
    nudgeTimer = setTimeout(pushHistory, 300)
    return
  }
  const step = e.shiftKey ? 10 : 1
  const map = { ArrowLeft: [-step, 0], ArrowRight: [step, 0], ArrowUp: [0, -step], ArrowDown: [0, step] }
  if (map[e.key]) {
    e.preventDefault()
    selected.value.posX += map[e.key][0]
    selected.value.posY += map[e.key][1]
    clearTimeout(nudgeTimer)
    nudgeTimer = setTimeout(pushHistory, 300)
  }
}
let nudgeTimer = null
onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))

/* ---------------- 保存 ---------------- */
async function save() {
  const berths = elements.value.filter((e) => e.elementType === 'BERTH')
  const codes = berths.map((b) => b.berthCode || berthMap.value[b.berthId]?.berthCode).filter(Boolean)
  if (new Set(codes).size !== codes.length) {
    ElMessage.warning('泊位编号不能重复')
    return
  }
  const payload = {
    portId: port.value.id,
    canvasWidth: canvas.width,
    canvasHeight: canvas.height,
    theme: canvas.theme,
    elements: [...elements.value].sort((a, b) => (a.layerIndex ?? 0) - (b.layerIndex ?? 0)).map(toPayload)
  }
  saving.value = true
  try {
    const res = confirmedWorking.value ? await portSaveMap({ ...payload, force: true }) : await withConfirm(portSaveMap, payload)
    if (res) {
      ElMessage.success('港口已保存')
      dirty.value = false
      emit('saved')
    }
  } finally {
    saving.value = false
  }
}
defineExpose({ isDirty: () => dirty.value })
</script>

<style scoped lang="scss">
.editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-radius: 16px;
  border: 1px solid var(--ght-line);
  overflow: hidden;
  box-shadow: 0 18px 40px -28px rgba(30, 58, 138, 0.45);
  &.is-full { position: fixed; inset: 0; z-index: 1999; border-radius: 0; border: none; }
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 14px;
  border-bottom: 1px solid var(--ght-line);
  gap: 10px;
  flex-wrap: wrap;
  background: linear-gradient(180deg, #fbfdff, #f1f6fd);
  position: relative;
  &::after { content: ''; position: absolute; left: 0; right: 0; bottom: -1px; height: 1px; background: linear-gradient(90deg, transparent, rgba(47, 91, 211, 0.55), transparent); }
}
.tool-group { display: flex; align-items: center; gap: 2px; }
.port-title { font-weight: 700; font-size: 15px; margin-right: 8px; letter-spacing: 0.5px; }
.zoom { min-width: 48px; text-align: center; color: var(--ght-ink-2); font-size: 13px; }
.body { flex: 1; display: flex; min-height: 0; }
.left { width: 188px; flex-shrink: 0; border-right: 1px solid var(--ght-line); overflow-x: hidden; overflow-y: auto; background: #f8fafe; }
.right { width: 280px; flex-shrink: 0; border-left: 1px solid var(--ght-line); overflow-x: hidden; overflow-y: auto; background: #f8fafe; }
/* 侧栏细滚动条：默认几乎隐形，悬停时显现 */
.left, .right {
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
  &:hover { scrollbar-color: rgba(47, 91, 211, 0.35) transparent; }
  &::-webkit-scrollbar { width: 6px; height: 0; }
  &::-webkit-scrollbar-thumb { background: transparent; border-radius: 6px; }
  &:hover::-webkit-scrollbar-thumb { background: rgba(47, 91, 211, 0.28); }
  &::-webkit-scrollbar-thumb:hover { background: rgba(47, 91, 211, 0.5); }
}
.stage { flex: 1; position: relative; min-width: 0; overflow: hidden; }
.legend {
  position: absolute; left: 14px; top: 12px; display: flex; gap: 12px; padding: 6px 12px; border-radius: 999px;
  background: rgba(255, 255, 255, 0.88); border: 1px solid rgba(47, 91, 211, 0.22); font-size: 12px; color: var(--ght-ink-2);
  box-shadow: 0 8px 20px -14px rgba(30, 58, 138, 0.6); backdrop-filter: blur(6px);
  i { display: inline-block; width: 10px; height: 10px; border-radius: 3px; margin-right: 5px; vertical-align: -1px; }
}
.op-tip {
  position: absolute; right: 14px; bottom: 12px; padding: 6px 12px; border-radius: 999px; font-size: 12px; color: var(--ght-ink-3);
  background: rgba(255, 255, 255, 0.8); border: 1px solid rgba(47, 91, 211, 0.16); pointer-events: none;
}
.empty-tip {
  position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center;
  pointer-events: none; color: var(--ght-blue); font-size: 14px; gap: 8px; .el-icon { font-size: 36px; }
}
.river-tip {
  position: absolute; top: 52px; left: 50%; transform: translateX(-50%); padding: 6px 14px; border-radius: 999px;
  background: rgba(30, 58, 138, 0.88); color: #fff; font-size: 12px; display: flex; align-items: center; gap: 6px; pointer-events: none;
}
@media (max-width: 1280px) { .op-tip { display: none; } }
</style>
