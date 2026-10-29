<template>
  <el-tooltip :content="menu.menuName" placement="right" :disabled="!collapsed">
    <router-link :to="menu.path" class="menu-link" active-class="active">
      <span class="icon"><el-icon><component :is="menu.icon || 'Grid'" /></el-icon></span>
      <span v-if="!collapsed" class="label">{{ menu.menuName }}</span>
    </router-link>
  </el-tooltip>
</template>

<script setup>
defineProps({
  menu: { type: Object, required: true },
  collapsed: Boolean
})
</script>

<style scoped lang="scss">
.menu-link {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  height: 44px;
  padding: 0 12px;
  margin: 2px 0;
  border-radius: 12px;
  color: #b9c6ea;
  font-size: 14px;
  transition: background 0.2s, color 0.2s;
  &:hover { background: rgba(255, 255, 255, 0.06); color: #fff; }
  &.active {
    color: #fff;
    background: linear-gradient(90deg, rgba(79, 127, 232, 0.95), rgba(34, 195, 230, 0.45));
    box-shadow: 0 8px 20px -8px rgba(47, 91, 211, 0.9), inset 0 0 0 1px rgba(255, 255, 255, 0.12);
    .icon { background: rgba(255, 255, 255, 0.18); }
    &::before {
      content: '';
      position: absolute;
      left: -12px;
      top: 10px;
      bottom: 10px;
      width: 3px;
      border-radius: 0 3px 3px 0;
      background: #7dd3fc;
      box-shadow: 0 0 10px #22c3e6;
    }
  }
}
.icon {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  font-size: 17px;
  flex-shrink: 0;
  transition: background 0.2s;
}
.label { white-space: nowrap; }
</style>
