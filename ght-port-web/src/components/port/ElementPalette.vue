<template>
  <div class="palette">
    <div v-for="(items, group) in groups" :key="group" class="group">
      <div class="group-name">{{ group }}</div>
      <div class="items">
        <div v-for="it in items" :key="it.type" class="item" draggable="true" :title="`拖动到画布：${it.name}`"
          @dragstart="onDragStart($event, it.type)" @dblclick="emit('add', it.type)">
          <div class="preview">
            <img v-if="thumbs[it.type]" :src="thumbs[it.type]" :alt="it.name" draggable="false" />
            <span v-else class="ph" />
          </div>
          <span>{{ it.name }}</span>
        </div>
      </div>
    </div>
    <div class="tip"><el-icon><InfoFilled /></el-icon> 拖拽或双击元素添加到场景</div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ELEMENT_TYPES } from './elementRegistry'
import { renderThumb } from '@/components/port3d/engine/thumbs'

const emit = defineEmits(['add'])
const thumbs = reactive({})
const groups = computed(() => {
  const g = {}
  Object.entries(ELEMENT_TYPES).forEach(([type, def]) => {
    ;(g[def.group] = g[def.group] || []).push({ type, ...def })
  })
  return g
})

/** 逐个渲染 3D 缩略图，避免一次性阻塞主线程 */
onMounted(() => {
  const list = Object.entries(ELEMENT_TYPES)
  const next = (i) => {
    if (i >= list.length) return
    const [type, def] = list[i]
    try {
      thumbs[type] = renderThumb(
        { uid: 'pal-' + type, elementType: type, width: def.w, height: def.h, cfg: def.cfg, berthCode: 'B1', berthName: '泊位' },
        { w: 128, h: 96, berth: type === 'BERTH' ? { status: 'WORKING' } : null }
      )
    } catch (e) {
      console.warn('缩略图渲染失败', type, e)
    }
    setTimeout(() => next(i + 1), 16)
  }
  next(0)
})

function onDragStart(e, type) {
  e.dataTransfer.setData('text/ght-element', type)
  e.dataTransfer.effectAllowed = 'copy'
}
</script>

<style scoped lang="scss">
.palette { padding: 12px; }
.group + .group { margin-top: 14px; }
.group-name { font-size: 11px; color: var(--ght-ink-3); letter-spacing: 2px; margin: 0 2px 8px; display: flex; align-items: center; gap: 6px;
  &::after { content: ''; flex: 1; height: 1px; background: linear-gradient(90deg, rgba(47, 91, 211, 0.25), transparent); } }
.items { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; }
.item {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  min-width: 0; padding: 6px 4px; border-radius: 10px; cursor: grab;
  background: #fff; border: 1px solid #e3eaf5; font-size: 12px; color: var(--ght-ink-2);
  transition: all 0.2s;
  &:hover { border-color: #9db7ef; box-shadow: 0 6px 16px -8px rgba(47, 91, 211, 0.55); transform: translateY(-1px); color: var(--ght-blue); }
  &:active { cursor: grabbing; }
}
.preview { width: 100%; max-width: 72px; aspect-ratio: 4 / 3; border-radius: 8px; background: linear-gradient(180deg, #f5f8fd, #e8eef8); display: grid; place-items: center; overflow: hidden;
  img { width: 100%; height: 100%; object-fit: contain; }
  .ph { width: 22px; height: 22px; border-radius: 6px; background: #dfe7f4; animation: ph 1.2s ease-in-out infinite; } }
@keyframes ph { 50% { opacity: 0.4; } }
.tip { margin-top: 14px; font-size: 12px; color: var(--ght-ink-3); display: flex; align-items: center; gap: 4px; }
</style>
