<template>
  <nav class="side-menu">
    <template v-for="m in menus" :key="m.id">
      <div v-if="m.menuType === 'DIR'" class="group">
        <div v-if="!collapsed" class="group-title">{{ m.menuName }}</div>
        <div v-else class="group-sep" />
        <MenuLink v-for="c in visibleChildren(m)" :key="c.id" :menu="c" :collapsed="collapsed" />
      </div>
      <MenuLink v-else-if="m.menuType === 'MENU'" :menu="m" :collapsed="collapsed" />
    </template>
  </nav>
</template>

<script setup>
import MenuLink from './MenuLink.vue'

defineProps({
  menus: { type: Array, default: () => [] },
  collapsed: Boolean
})
const visibleChildren = (m) => (m.children || []).filter((c) => c.menuType === 'MENU')
</script>

<style scoped lang="scss">
.side-menu { padding: 6px 12px 20px; }
.group { margin-top: 12px; }
.group-title {
  font-size: 11px;
  letter-spacing: 2px;
  color: #6d81bb;
  padding: 10px 12px 6px;
}
.group-sep { height: 1px; background: rgba(255, 255, 255, 0.08); margin: 10px 8px; }
</style>
