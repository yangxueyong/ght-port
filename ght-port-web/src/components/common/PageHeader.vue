<template>
  <div class="page-header">
    <div class="titles">
      <div class="title">
        <span class="icon" v-if="icon"><el-icon><component :is="icon" /></el-icon></span>
        {{ title }}
      </div>
      <div v-if="subtitle" class="subtitle">{{ subtitle }}</div>
    </div>
    <div class="actions">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({ title: String, subtitle: String, icon: String })
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}
.title { font-size: 22px; font-weight: 700; display: flex; align-items: center; gap: 12px; letter-spacing: 1px; }
.icon {
  position: relative;
  width: 38px; height: 38px; border-radius: 11px; display: grid; place-items: center; font-size: 18px;
  color: #fff; background: linear-gradient(135deg, #4f7fe8, #1e3a8a);
  box-shadow: 0 8px 18px -8px rgba(30, 58, 138, 0.8);
  &::after {
    content: ''; position: absolute; inset: -4px; border-radius: 14px; border: 1px dashed rgba(47, 91, 211, 0.45);
    animation: orbit 14s linear infinite;
  }
}
@keyframes orbit { to { transform: rotate(360deg); } }
.subtitle {
  color: var(--ght-ink-3); margin-top: 8px; font-size: 13px; padding-left: 50px; position: relative;
  &::before { content: ''; position: absolute; left: 0; top: 50%; width: 38px; height: 1px; background: linear-gradient(90deg, var(--ght-tech), transparent); }
}
.actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
</style>
