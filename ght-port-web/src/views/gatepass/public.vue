<template>
  <div class="mobile-page">
    <div class="top">
      <img src="@/assets/logo.png" alt="" />
      <div>
        <div class="brand">港慧通</div>
        <div class="slogan">让多式联运通达顺远</div>
      </div>
    </div>
    <div v-if="loading" class="state"><el-icon class="spin"><Loading /></el-icon> 正在加载出门条…</div>
    <div v-else-if="error" class="state error">
      <el-icon><WarningFilled /></el-icon>
      <p>{{ error }}</p>
    </div>
    <template v-else-if="pass">
      <PassTicket :pass="pass" />
      <div class="foot">
        <el-button round icon="Refresh" @click="load">刷新状态</el-button>
        <p>出门时请调亮屏幕，向门卫出示二维码。二维码核销后即失效，不能重复使用。</p>
      </div>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import PassTicket from './components/PassTicket.vue'
import { publicGatePass } from '@/api/auth'

const route = useRoute()
const pass = ref(null)
const loading = ref(true)
const error = ref('')
async function load() {
  loading.value = true
  error.value = ''
  try {
    pass.value = await publicGatePass(route.params.token)
  } catch (e) {
    error.value = e.message || '出门条不存在或链接已失效'
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped lang="scss">
.mobile-page {
  min-height: 100vh; max-width: 480px; margin: 0 auto; padding: 18px 16px 32px;
  background: radial-gradient(120% 50% at 50% 0%, #dbe9ff 0%, transparent 70%), var(--ght-bg);
}
.top { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; img { width: 40px; height: 40px; border-radius: 12px; background: #fff; padding: 4px; box-shadow: var(--ght-shadow-sm); } }
.brand { font-weight: 800; font-size: 18px; color: var(--ght-navy); letter-spacing: 2px; }
.slogan { font-size: 12px; color: var(--ght-ink-3); }
.state { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 80px 0; color: var(--ght-ink-3); .el-icon { font-size: 36px; } &.error { color: var(--ght-maint); } }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.foot { text-align: center; margin-top: 18px; p { font-size: 12px; color: var(--ght-ink-3); line-height: 1.7; margin-top: 12px; } }
</style>
