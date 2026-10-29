<template>
  <div class="err">
    <div class="code">403</div>
    <p>当前账号没有可访问的页面，请联系管理员分配权限</p>
    <el-button type="primary" @click="logout">重新登录</el-button>
  </div>
</template>
<script setup>
import { useUserStore } from '@/stores/user'
const store = useUserStore()
function logout() {
  store.logout()
  location.hash = '#/login'
  location.reload()
}
</script>
<style scoped>
.err { min-height: 70vh; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; color: var(--ght-ink-3); }
.code { font-size: 96px; font-weight: 800; background: linear-gradient(135deg, #1f7ae0, #182d86); -webkit-background-clip: text; background-clip: text; color: transparent; }
</style>
