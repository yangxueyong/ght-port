<template>
  <div class="mobile-page">
    <div class="top">
      <el-button circle icon="ArrowLeft" @click="back" />
      <div class="title">出门条核销</div>
      <span class="guard">{{ store.info?.nickname }}</span>
    </div>

    <div v-if="loading" class="state"><el-icon class="spin"><Loading /></el-icon> 正在校验出门条…</div>
    <div v-else-if="error" class="result fail">
      <div class="icon"><el-icon><CircleCloseFilled /></el-icon></div>
      <div class="r-title">无法核销</div>
      <p>{{ error }}</p>
    </div>
    <template v-else-if="pass">
      <div v-if="justVerified" class="result ok">
        <div class="icon"><el-icon><CircleCheckFilled /></el-icon></div>
        <div class="r-title">核销成功，准予放行</div>
        <p class="num">{{ pass.plateNo }} · {{ fmtTime(pass.verifyTime, 'HH:mm:ss') }}</p>
      </div>
      <div v-else-if="pass.status === 'VERIFIED'" class="result fail">
        <div class="icon"><el-icon><WarningFilled /></el-icon></div>
        <div class="r-title">该出门条已核销</div>
        <p>已于 {{ fmtTime(pass.verifyTime, 'MM-DD HH:mm:ss') }} 由 {{ pass.verifyBy }} 核销，二维码不能重复使用</p>
      </div>
      <PassTicket :pass="pass" :show-qr="false" />
      <el-button v-if="pass.status === 'UNUSED'" type="primary" size="large" class="verify-btn" :loading="verifying" @click="verify">
        <el-icon><Select /></el-icon> 确认核销并放行
      </el-button>
      <el-button v-else size="large" class="verify-btn" @click="back">继续扫码</el-button>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PassTicket from './components/PassTicket.vue'
import { gatePassPreview, gatePassVerify } from '@/api/gatepass'
import { fmtTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const pass = ref(null)
const loading = ref(true)
const verifying = ref(false)
const justVerified = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  try {
    pass.value = await gatePassPreview(route.params.token)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
async function verify() {
  verifying.value = true
  try {
    pass.value = await gatePassVerify(route.params.token)
    justVerified.value = true
    if (navigator.vibrate) navigator.vibrate(120)
  } catch (e) {
    error.value = e.message
    pass.value = null
  } finally {
    verifying.value = false
  }
}
function back() {
  router.push('/gate/scan')
}
onMounted(load)
</script>

<style scoped lang="scss">
.mobile-page { min-height: 100vh; max-width: 480px; margin: 0 auto; padding: 14px 16px 32px; background: var(--ght-bg); }
.top { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; .title { font-weight: 700; font-size: 17px; flex: 1; } .guard { font-size: 12px; color: var(--ght-ink-3); } }
.state { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 80px 0; color: var(--ght-ink-3); .el-icon { font-size: 36px; } }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.result {
  text-align: center; padding: 22px 16px; border-radius: 18px; margin-bottom: 14px; color: #fff;
  .icon { font-size: 54px; line-height: 1; } .r-title { font-size: 20px; font-weight: 800; margin-top: 8px; } p { margin: 8px 0 0; opacity: 0.9; font-size: 13px; }
  &.ok { background: linear-gradient(135deg, #0b9e6f, #12b886); box-shadow: 0 16px 30px -16px #12b886; .icon { animation: pop 0.5s cubic-bezier(0.2, 1.6, 0.4, 1); } }
  &.fail { background: linear-gradient(135deg, #b4282d, #e5484d); box-shadow: 0 16px 30px -16px #e5484d; }
}
@keyframes pop { from { transform: scale(0.3); opacity: 0; } to { transform: scale(1); opacity: 1; } }
.verify-btn { width: 100%; margin-top: 16px; height: 50px; font-size: 16px; border-radius: 14px; }
</style>
