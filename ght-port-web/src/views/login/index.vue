<template>
  <div class="login">
    <section class="hero">
      <div class="hero-top">
        <div class="logo"><img src="@/assets/logo.png" alt="港慧通" /></div>
        <div>
          <div class="brand">港慧通</div>
          <div class="brand-en">GANG HUI TONG · SMART PORT LOGISTICS</div>
        </div>
      </div>
      <div class="hero-copy">
        <h1>让多式联运<br /><span>通达顺远</span></h1>
        <p>泊位作业、承运调度、电子出门条，一张俯视图掌握港口全局。</p>
      </div>
      <svg class="scene" viewBox="0 0 800 360" preserveAspectRatio="xMidYMax slice">
        <defs>
          <linearGradient id="lgRiver" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0" stop-color="#2f7fe0" stop-opacity="0.9" />
            <stop offset="1" stop-color="#123a9a" stop-opacity="0.95" />
          </linearGradient>
          <linearGradient id="lgDock" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0" stop-color="#3a4f9a" />
            <stop offset="1" stop-color="#22336f" />
          </linearGradient>
        </defs>
        <!-- 远山 -->
        <path d="M0 200 L90 150 L170 185 L260 130 L360 180 L450 140 L560 190 L650 150 L800 190 V360 H0Z" fill="rgba(255,255,255,0.05)" />
        <!-- 门机 -->
        <g class="crane" transform="translate(520 92)">
          <rect x="0" y="40" width="10" height="120" fill="#f5a524" />
          <rect x="60" y="40" width="10" height="120" fill="#f5a524" />
          <rect x="-50" y="30" width="190" height="12" fill="#ffb84d" />
          <g class="trolley"><rect x="-30" y="42" width="22" height="10" fill="#22336f" /><line x1="-19" y1="52" x2="-19" y2="100" stroke="#cfe0ff" stroke-width="1.5" /><rect x="-32" y="100" width="26" height="12" fill="#e67e22" /></g>
        </g>
        <!-- 码头 -->
        <rect x="380" y="250" width="420" height="40" fill="url(#lgDock)" />
        <g fill="#e67e22"><rect x="600" y="226" width="44" height="24" /><rect x="646" y="226" width="44" height="24" fill="#1f7ae0" /><rect x="622" y="202" width="44" height="24" fill="#27ae60" /></g>
        <!-- 卡车 -->
        <g class="truck">
          <rect x="0" y="232" width="56" height="18" rx="2" fill="#ffffff" />
          <rect x="58" y="236" width="18" height="14" rx="2" fill="#22c3e6" />
          <circle cx="14" cy="252" r="5" fill="#0b1847" /><circle cx="44" cy="252" r="5" fill="#0b1847" /><circle cx="68" cy="252" r="5" fill="#0b1847" />
        </g>
        <!-- 江面 -->
        <rect x="0" y="290" width="800" height="70" fill="url(#lgRiver)" />
        <g class="waves" stroke="rgba(255,255,255,0.35)" stroke-width="2" fill="none">
          <path d="M0 310 q20 -8 40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0" />
          <path d="M-20 334 q20 -8 40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0 t40 0" opacity="0.6" />
        </g>
        <!-- 船 -->
        <g class="ship">
          <path d="M0 290 H200 L184 314 H18 Z" fill="#ffffff" />
          <rect x="16" y="262" width="40" height="28" fill="#dfe8ff" /><rect x="22" y="268" width="28" height="6" fill="#2f7fe0" />
          <g><rect x="64" y="270" width="26" height="20" fill="#e67e22" /><rect x="92" y="270" width="26" height="20" fill="#27ae60" /><rect x="120" y="270" width="26" height="20" fill="#f1c40f" /><rect x="148" y="270" width="26" height="20" fill="#e84393" /><rect x="78" y="250" width="26" height="20" fill="#22c3e6" /><rect x="106" y="250" width="26" height="20" fill="#8e44ad" /></g>
        </g>
      </svg>
    </section>

    <section class="panel">
      <div class="card">
        <div class="card-title">欢迎登录</div>
        <div class="card-sub">港口物流管理系统</div>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
          <el-form-item prop="tenantCode">
            <el-select v-model="form.tenantCode" placeholder="请选择所属港口" style="width: 100%">
              <template #prefix><el-icon><Location /></el-icon></template>
              <el-option v-for="t in tenants" :key="t.tenantCode" :label="t.tenantName" :value="t.tenantCode" />
            </el-select>
          </el-form-item>
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="账号 / 手机号" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="密码" prefix-icon="Lock" />
          </el-form-item>
          <el-button type="primary" class="submit" :loading="loading" @click="submit">登 录</el-button>
        </el-form>
        <div class="demo">
          <div class="demo-title">演示账号（密码均为 123456）</div>
          <div class="demo-list">
            <span v-for="a in demos" :key="a.u" @click="fill(a)">{{ a.label }}<em>{{ a.u }}</em></span>
          </div>
        </div>
      </div>
      <div class="copyright">© 港慧通 · 让多式联运通达顺远</div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { tenantOptions } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { getLastTenant } from '@/utils/storage'

const router = useRouter()
const route = useRoute()
const store = useUserStore()
const formRef = ref()
const loading = ref(false)
const tenants = ref([])
const form = reactive({ tenantCode: getLastTenant() || 'fllt', username: '', password: '' })
const rules = {
  tenantCode: [{ required: true, message: '请选择所属港口' }],
  username: [{ required: true, message: '请输入账号' }],
  password: [{ required: true, message: '请输入密码' }]
}
const demos = [
  { label: '港口管理员', u: 'admin', t: 'fllt' },
  { label: '调度员', u: 'diaodu', t: 'fllt' },
  { label: '门卫', u: 'menwei', t: 'fllt' },
  { label: '承运商', u: '13800000001', t: 'fllt' },
  { label: '司机', u: '13900000001', t: 'fllt' },
  { label: '平台超管', u: 'admin', t: 'platform' }
]
function fill(a) {
  form.tenantCode = a.t
  form.username = a.u
  form.password = '123456'
}
async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await store.login(form)
    store.routesReady = false
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}
onMounted(async () => {
  try {
    tenants.value = await tenantOptions()
  } catch (e) {
    tenants.value = []
  }
})
</script>

<style scoped lang="scss">
.login { display: flex; min-height: 100vh; background: #f3f6fb; }
.hero {
  position: relative; flex: 1.25; overflow: hidden; color: #fff; padding: 40px 56px;
  background: radial-gradient(80% 60% at 20% 10%, #2a4fc0 0%, transparent 60%), radial-gradient(60% 50% at 90% 30%, #0a64b0 0%, transparent 60%), linear-gradient(160deg, #0f2366, #0b1847);
}
.hero-top { position: relative; z-index: 1; display: flex; align-items: center; gap: 14px; }
.logo { width: 52px; height: 52px; border-radius: 15px; background: #fff; display: grid; place-items: center; box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.6); img { width: 42px; height: 42px; object-fit: contain; } }
.brand { font-size: 24px; font-weight: 800; letter-spacing: 4px; }
.brand-en { font-size: 11px; letter-spacing: 3px; opacity: 0.6; margin-top: 2px; }
.hero-copy { position: relative; z-index: 1; margin-top: 12vh; h1 { font-size: 46px; line-height: 1.25; margin: 0; letter-spacing: 4px; span { background: linear-gradient(90deg, #7fd3ff, #ffffff); -webkit-background-clip: text; background-clip: text; color: transparent; } } p { font-size: 16px; opacity: 0.75; margin-top: 18px; letter-spacing: 1px; } }
.scene { position: absolute; left: 0; right: 0; bottom: 0; width: 100%; height: 46%; }
.waves path { animation: waves 6s linear infinite; }
@keyframes waves { to { transform: translateX(-80px); } }
.ship { animation: ship 22s linear infinite; }
@keyframes ship { from { transform: translateX(-260px); } to { transform: translateX(820px); } }
.truck { animation: truck 9s linear infinite; }
@keyframes truck { from { transform: translateX(370px); } to { transform: translateX(820px); } }
.trolley { animation: trolley 6s ease-in-out infinite; }
@keyframes trolley { 0%, 100% { transform: translateX(0); } 50% { transform: translateX(120px); } }
.panel { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 32px; position: relative; }
.card { width: 100%; max-width: 400px; background: #fff; border-radius: 22px; padding: 36px 34px 26px; box-shadow: 0 30px 60px -30px rgba(11, 24, 71, 0.35); border: 1px solid #e6ebf3; }
.card-title { font-size: 26px; font-weight: 800; }
.card-sub { color: var(--ght-ink-3); margin: 6px 0 26px; }
.submit { width: 100%; height: 46px; font-size: 16px; border-radius: 12px; letter-spacing: 4px; background: linear-gradient(90deg, #1f7ae0, #182d86); border: 0; box-shadow: 0 12px 24px -12px #1f7ae0; }
.demo { margin-top: 22px; border-top: 1px dashed #e1e7f0; padding-top: 16px; }
.demo-title { font-size: 12px; color: var(--ght-ink-3); margin-bottom: 10px; }
.demo-list { display: flex; flex-wrap: wrap; gap: 8px; span { cursor: pointer; font-size: 12px; padding: 4px 10px; border-radius: 999px; background: #f3f8ff; color: var(--ght-blue); transition: all 0.15s; em { font-style: normal; color: var(--ght-ink-3); margin-left: 4px; } &:hover { background: #e3eefc; } } }
.copyright { position: absolute; bottom: 20px; font-size: 12px; color: var(--ght-ink-3); }
@media (max-width: 960px) { .hero { display: none; } }
</style>
