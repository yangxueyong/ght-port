<template>
  <div class="layout" :class="{ collapsed }">
    <aside class="sidebar">
      <div class="brand">
        <div class="logo-badge"><img src="@/assets/logo.png" alt="港慧通" /></div>
        <transition name="fade">
          <div v-if="!collapsed" class="brand-text">
            <div class="brand-name">港慧通</div>
            <div class="brand-slogan">让多式联运通达顺远</div>
          </div>
        </transition>
      </div>
      <el-scrollbar class="menu-scroll">
        <SideMenu :menus="userStore.menus" :collapsed="collapsed" />
      </el-scrollbar>
      <div class="side-foot" @click="collapsed = !collapsed">
        <el-icon><component :is="collapsed ? 'Expand' : 'Fold'" /></el-icon>
        <span v-if="!collapsed">收起菜单</span>
      </div>
      <svg class="side-wave" viewBox="0 0 240 80" preserveAspectRatio="none">
        <path d="M0 40 Q 30 20 60 40 T 120 40 T 180 40 T 240 40 V80 H0Z" fill="rgba(255,255,255,0.04)">
          <animate attributeName="d" dur="6s" repeatCount="indefinite"
            values="M0 40 Q 30 20 60 40 T 120 40 T 180 40 T 240 40 V80 H0Z;M0 40 Q 30 60 60 40 T 120 40 T 180 40 T 240 40 V80 H0Z;M0 40 Q 30 20 60 40 T 120 40 T 180 40 T 240 40 V80 H0Z" />
        </path>
      </svg>
    </aside>
    <section class="main">
      <TopBar />
      <main class="content">
        <router-view v-slot="{ Component, route }">
          <transition name="page" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </main>
    </section>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import SideMenu from './SideMenu.vue'
import TopBar from './TopBar.vue'
import { getItem, setItem } from '@/utils/storage'

const userStore = useUserStore()
const collapsed = ref(getItem('ght_collapsed') === '1')
watch(collapsed, (v) => setItem('ght_collapsed', v ? '1' : '0'))
</script>

<style scoped lang="scss">
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  --side-w: 232px;
  &.collapsed { --side-w: 72px; }
}
.sidebar {
  position: relative;
  width: var(--side-w);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background:
    linear-gradient(rgba(125, 160, 255, 0.06) 1px, transparent 1px) 0 0 / 24px 24px,
    linear-gradient(90deg, rgba(125, 160, 255, 0.06) 1px, transparent 1px) 0 0 / 24px 24px,
    radial-gradient(120% 60% at 0% 0%, #2a4cc0 0%, transparent 60%),
    linear-gradient(180deg, #13277a 0%, #0b1847 100%);
  color: #cdd7f2;
  transition: width 0.28s cubic-bezier(0.2, 0.8, 0.2, 1);
  overflow: hidden;
  z-index: 10;
  // 右侧科技光边
  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    width: 1px;
    background: linear-gradient(180deg, transparent, rgba(34, 195, 230, 0.7) 30%, rgba(79, 127, 232, 0.5) 70%, transparent);
  }
}
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 18px 18px;
  height: 78px;
  position: relative;
  &::after {
    content: '';
    position: absolute;
    left: 18px;
    right: 18px;
    bottom: 0;
    height: 1px;
    background: linear-gradient(90deg, rgba(34, 195, 230, 0.6), transparent);
  }
}
.logo-badge {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  background: #fff;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  box-shadow: 0 6px 16px -4px rgba(0, 0, 0, 0.4), inset 0 0 0 1px rgba(255, 255, 255, 0.6);
  img { width: 30px; height: 30px; object-fit: contain; }
}
.brand-name { font-size: 18px; font-weight: 700; color: #fff; letter-spacing: 2px; }
.brand-slogan { font-size: 11px; color: #8fa3d9; margin-top: 2px; white-space: nowrap; letter-spacing: 1px; }
.menu-scroll { flex: 1; }
.side-foot {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 24px;
  cursor: pointer;
  color: #8fa3d9;
  font-size: 13px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  position: relative;
  z-index: 1;
  &:hover { color: #fff; }
}
.side-wave { position: absolute; left: 0; right: 0; bottom: 0; height: 80px; width: 100%; pointer-events: none; }
.main { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.content { flex: 1; overflow: auto; }
.page-enter-active, .page-leave-active { transition: opacity 0.18s ease, transform 0.18s ease; }
.page-enter-from { opacity: 0; transform: translateY(6px); }
.page-leave-to { opacity: 0; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
