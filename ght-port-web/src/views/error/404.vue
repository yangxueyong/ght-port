<template>
  <div class="err">
    <div class="code">404</div>
    <p>页面不存在或无权访问</p>
    <el-button type="primary" @click="$router.replace('/')">返回首页</el-button>
  </div>
</template>
<style scoped>
.err { min-height: 70vh; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; color: var(--ght-ink-3); }
.code { font-size: 96px; font-weight: 800; background: linear-gradient(135deg, #1f7ae0, #182d86); -webkit-background-clip: text; background-clip: text; color: transparent; }
</style>
