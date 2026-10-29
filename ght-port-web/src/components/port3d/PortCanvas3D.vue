<template>
  <div ref="hostRef" class="port3d" :class="`mode-${mode}`">
    <div v-if="failed" class="fail">
      <el-icon><WarningFilled /></el-icon>
      <p>当前浏览器不支持 WebGL，无法显示 3D 港口</p>
    </div>
    <!-- 定位虚化：除当前定位的泊位外，整个场景蒙上一层毛玻璃（椭圆清晰区随镜头实时跟随） -->
    <div v-if="focusBlur && !failed" ref="blurRef" class="focus-blur" />
    <div v-if="viewSwitch && !failed" class="view-switch">
      <span v-for="v in VIEW_OPTIONS" :key="v.value" :class="{ on: curView === v.value }" @click="fit(v.value)">{{ v.label }}</span>
    </div>
    <slot />
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import { PortScene } from './engine/PortScene'

/**
 * 3D 港口画布：封装 PortScene，向外抛出拾取/拖拽事件
 * mode: edit 编辑（可拖拽元素与手柄）| berth 泊位看板（泊位可点击）| view 只读展示
 */
const props = defineProps({
  elements: { type: Array, default: () => [] },
  berthMap: { type: Object, default: () => ({}) },
  width: { type: Number, default: 1600 },
  height: { type: Number, default: 900 },
  theme: { type: String, default: 'DAY' },
  mode: { type: String, default: 'view' },
  selectedUid: { type: String, default: null },
  autoRotate: Boolean,
  /** 初始视角：iso 斜视 / top 俯视（全量） / front 平视 */
  initialView: { type: String, default: 'iso' },
  /** 是否显示右上角视角切换 */
  viewSwitch: Boolean,
  /** 被浮层遮挡的边距（像素），画面焦点居中于未遮挡区域 */
  insets: { type: Object, default: null },
  /** 定位泊位时，周围场景虚化（毛玻璃） */
  focusBlur: Boolean,
  /** 记忆视角的键：设置后自动保存用户的视角、角度、缩放，下次打开时还原 */
  viewKey: { type: String, default: '' }
})
const STORE_PREFIX = 'ght:port3d:view:'
/** 读取记忆的视角 */
function readView(key) {
  if (!key) return null
  try {
    return JSON.parse(localStorage.getItem(STORE_PREFIX + key) || 'null')
  } catch {
    return null
  }
}
/** 保存当前视角 */
function saveView() {
  clearTimeout(saveTimer)
  saveTimer = null
  if (!props.viewKey || !scene.value) return
  try {
    localStorage.setItem(STORE_PREFIX + props.viewKey, JSON.stringify(scene.value.getCamera()))
  } catch {
    /* 存储不可用时忽略 */
  }
}
let saveTimer = null
/** 镜头变化后稍作延迟再保存（阻尼惯性结束后才是最终位置） */
function queueSave() {
  if (!props.viewKey) return
  clearTimeout(saveTimer)
  saveTimer = setTimeout(saveView, 400)
}
/** 还原记忆的视角，没有记忆时按初始视角适应 */
function restoreView() {
  const s = readView(props.viewKey)
  if (s && scene.value?.setCamera(s)) curView.value = s.view || props.initialView
  else fit(props.initialView)
}
const blurRef = ref()
/** 每帧把定位元素的屏幕椭圆写入遮罩（只改 CSS 变量，开销很小） */
function syncBlur() {
  const el = blurRef.value
  if (!el || !scene.value) return
  const r = scene.value.focusScreenRect()
  if (!r) {
    el.style.opacity = '0'
    return
  }
  const pad = 36
  el.style.opacity = '1'
  el.style.setProperty('--cx', `${r.cx.toFixed(1)}px`)
  el.style.setProperty('--cy', `${r.cy.toFixed(1)}px`)
  el.style.setProperty('--rx', `${(r.rx * 1.08 + pad).toFixed(1)}px`)
  el.style.setProperty('--ry', `${(r.ry * 1.12 + pad).toFixed(1)}px`)
}
const VIEW_OPTIONS = [
  { label: '俯视', value: 'top' },
  { label: '斜视', value: 'iso' }
]
const curView = ref(props.initialView)
const emit = defineEmits(['el-pointerdown', 'handle-pointerdown', 'handle-dblclick', 'bg-click', 'el-click', 'el-dblclick', 'berth-click', 'view-change'])
const hostRef = ref()
const scene = shallowRef(null)
const failed = ref(false)

const findEl = (uid) => props.elements.find((e) => e.uid === uid)

function push() {
  scene.value?.setData({ elements: props.elements, berthMap: props.berthMap, width: props.width, height: props.height, theme: props.theme })
}

onMounted(() => {
  try {
    scene.value = new PortScene(hostRef.value, {
      mode: props.mode,
      autoRotate: props.autoRotate,
      handlers: {
        onElementDown: (evt, uid) => findEl(uid) && emit('el-pointerdown', evt, findEl(uid)),
        onHandleDown: (evt, kind, idx) => emit('handle-pointerdown', evt, kind, idx),
        onHandleDblClick: (evt, kind, idx) => emit('handle-dblclick', evt, kind, idx),
        onBgClick: (evt) => emit('bg-click', evt),
        onDblClick: (evt, uid) => findEl(uid) && emit('el-dblclick', evt, findEl(uid)),
        onElementClick: (uid) => {
          const el = findEl(uid)
          if (!el) return
          emit('el-click', el)
          const b = el.berthId ? props.berthMap[el.berthId] : null
          if (el.elementType === 'BERTH' && b) emit('berth-click', b, el)
        },
        onViewChange: () => {
          queueSave()
          emit('view-change')
        },
        onAfterFrame: () => props.focusBlur && syncBlur()
      }
    })
    push()
    if (props.insets) scene.value.setInsets(props.insets)
    scene.value.setSelected(props.selectedUid)
    restoreView()
  } catch (e) {
    console.error(e)
    failed.value = true
  }
})
onBeforeUnmount(() => {
  if (saveTimer) saveView()
  scene.value?.dispose()
})
// 切换港口等场景时，先保存旧键的视角，再还原新键的视角
watch(
  () => props.viewKey,
  async (key, old) => {
    if (!scene.value) return
    if (saveTimer && old) {
      clearTimeout(saveTimer)
      saveTimer = null
      try {
        localStorage.setItem(STORE_PREFIX + old, JSON.stringify(scene.value.getCamera()))
      } catch {
        /* 忽略 */
      }
    }
    await nextTick()
    restoreView()
  }
)

watch(() => [props.elements, props.berthMap, props.width, props.height, props.theme], push, { deep: true })
watch(() => props.selectedUid, (v) => scene.value?.setSelected(v))
watch(
  () => props.insets,
  (v) => {
    scene.value?.setInsets(v)
    if (scene.value && !scene.value.userMoved) scene.value.fit(curView.value)
  },
  { deep: true }
)
watch(() => props.mode, (v) => scene.value?.setMode(v))
watch(
  () => props.autoRotate,
  (v) => {
    if (scene.value) scene.value.controls.autoRotate = !!v
  }
)

/** 屏幕坐标 -> 画布坐标 */
const toCanvasPoint = (x, y) => scene.value?.toCanvasPoint(x, y) || null
/** 视角：iso 斜视 / top 俯视 / front 平视 */
const fit = (view) => {
  curView.value = view
  scene.value?.fit(view)
}
const zoomBy = (f) => scene.value?.zoomBy(f)
const zoomPct = () => scene.value?.zoomPct() || 100
/** 1 个屏幕像素对应的世界单位 */
const worldPerPixel = () => scene.value?.worldPerPixel() || 1
/** 镜头定位到泊位（berthId 为空时恢复全景） */
const focusBerth = (berthId) => {
  const el = berthId ? props.elements.find((e) => e.berthId === berthId) : null
  scene.value?.focusUid(el ? el.uid : null)
}
const centerPoint = () => scene.value?.centerCanvasPoint() || { x: props.width / 2, y: props.height / 2 }
defineExpose({ toCanvasPoint, fit, zoomBy, zoomPct, centerPoint, worldPerPixel, focusBerth })
</script>

<style scoped lang="scss">
.port3d {
  position: relative;
  /* 独立层叠上下文：内部的虚化遮罩、视角按钮不会盖到页面其他浮层上 */
  isolation: isolate;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: radial-gradient(120% 90% at 50% 30%, #f6f9fd 0%, #e6edf7 60%, #dbe4f1 100%);
  user-select: none;
  touch-action: none;
}
.focus-blur {
  --cx: 50%; --cy: 50%; --rx: 200px; --ry: 140px;
  position: absolute; inset: 0; z-index: 1; pointer-events: none; opacity: 0; transition: opacity 0.5s;
  background: rgba(236, 243, 255, 0.3);
  backdrop-filter: blur(5px) saturate(110%); -webkit-backdrop-filter: blur(5px) saturate(110%);
  mask-image: radial-gradient(ellipse var(--rx) var(--ry) at var(--cx) var(--cy), transparent 72%, #000 100%);
  -webkit-mask-image: radial-gradient(ellipse var(--rx) var(--ry) at var(--cx) var(--cy), transparent 72%, #000 100%);
}
.view-switch {
  position: absolute; right: 12px; top: 12px; z-index: 2; display: flex; padding: 3px; gap: 2px; border-radius: 10px;
  background: rgba(255, 255, 255, 0.86); backdrop-filter: blur(6px); box-shadow: 0 4px 14px -6px rgba(15, 42, 107, 0.35);
  span { padding: 3px 12px; font-size: 12px; border-radius: 7px; cursor: pointer; color: var(--ght-ink-2); transition: all 0.15s;
    &:hover { color: var(--ght-blue); } &.on { background: var(--ght-blue); color: #fff; } }
}
.fail {
  position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: var(--ght-ink-3); gap: 6px; .el-icon { font-size: 32px; color: var(--ght-working); }
}
</style>
