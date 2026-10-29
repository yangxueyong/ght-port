<template>
  <span class="num">{{ text }}</span>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'

/** 数字滚动：数值变化时从旧值平滑过渡到新值 */
const props = defineProps({ value: { type: [Number, String], default: 0 }, digits: { type: Number, default: 0 }, duration: { type: Number, default: 900 } })
const shown = ref(0)
let raf = 0
const text = computed(() => shown.value.toLocaleString('zh-CN', { maximumFractionDigits: props.digits, minimumFractionDigits: 0 }))

watch(
  () => Number(props.value) || 0,
  (to) => {
    cancelAnimationFrame(raf)
    const from = shown.value
    const t0 = performance.now()
    const step = (t) => {
      const p = Math.min(1, (t - t0) / props.duration)
      const e = 1 - Math.pow(1 - p, 3)
      shown.value = from + (to - from) * e
      if (p < 1) raf = requestAnimationFrame(step)
    }
    raf = requestAnimationFrame(step)
  },
  { immediate: true }
)
onBeforeUnmount(() => cancelAnimationFrame(raf))
</script>
