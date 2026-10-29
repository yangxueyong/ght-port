<template>
  <div class="stat-tile tech-corners" :style="{ '--accent': color }">
    <div class="icon"><el-icon><component :is="icon" /></el-icon></div>
    <div class="body">
      <div class="label">{{ label }}</div>
      <div class="value num">{{ value }}<small v-if="unit">{{ unit }}</small></div>
      <div v-if="hint" class="hint">{{ hint }}</div>
    </div>
    <div class="glow" />
  </div>
</template>

<script setup>
defineProps({
  label: String,
  value: [String, Number],
  unit: String,
  hint: String,
  icon: { type: String, default: 'DataLine' },
  color: { type: String, default: '#1f7ae0' }
})
</script>

<style scoped lang="scss">
.stat-tile {
  position: relative;
  overflow: hidden;
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 18px 18px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid var(--ght-line);
  box-shadow: 0 12px 30px -24px rgba(30, 58, 138, 0.45);
  &::after {
    content: ''; position: absolute; left: 0; right: 0; bottom: 0; height: 2px;
    background: linear-gradient(90deg, var(--accent), transparent 70%); opacity: 0.7;
  }
}
.icon {
  width: 46px; height: 46px; border-radius: 14px; display: grid; place-items: center; font-size: 22px;
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 12%, white);
}
.label { color: var(--ght-ink-3); font-size: 13px; }
.value { font-size: 28px; font-weight: 700; margin-top: 2px; line-height: 1.1; small { font-size: 13px; color: var(--ght-ink-3); margin-left: 4px; font-weight: 500; } }
.hint { font-size: 12px; color: var(--ght-ink-3); margin-top: 4px; }
.glow {
  position: absolute; right: -30px; top: -30px; width: 110px; height: 110px; border-radius: 50%;
  background: radial-gradient(circle, color-mix(in srgb, var(--accent) 22%, transparent), transparent 70%);
}
</style>
