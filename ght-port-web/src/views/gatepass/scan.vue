<template>
  <div class="ght-page">
    <PageHeader title="扫码核销" icon="Aim" subtitle="门卫使用手机扫描司机出示的出门条二维码，核销后放行；已核销的二维码不能再次使用" />
    <div class="scan-layout">
      <div class="ght-card scanner">
        <div class="frame">
          <div id="ght-qr-reader" class="reader" />
          <div v-if="!scanning" class="placeholder">
            <svg viewBox="0 0 120 120" width="110" height="110">
              <path d="M10 34 V10 H34 M86 10 H110 V34 M110 86 V110 H86 M34 110 H10 V86" stroke="#1f7ae0" stroke-width="6" fill="none" stroke-linecap="round" />
              <rect x="34" y="34" width="52" height="52" rx="6" fill="#e8f1fd" />
              <line class="laser" x1="22" x2="98" y1="60" y2="60" stroke="#22c3e6" stroke-width="3" />
            </svg>
            <p>点击下方按钮开启摄像头扫码</p>
          </div>
        </div>
        <div class="btns">
          <el-button v-if="!scanning" type="primary" size="large" icon="Camera" @click="start">开启扫码</el-button>
          <el-button v-else size="large" icon="VideoPause" @click="stop">停止扫码</el-button>
        </div>
        <p class="tip">也可以直接用手机自带相机/微信扫一扫扫描二维码，登录后即可核销。</p>
      </div>
      <div class="ght-card manual">
        <div class="section-title">手动输入</div>
        <p class="muted">二维码无法识别时，可输入出门条链接或令牌</p>
        <el-input v-model="manual" placeholder="粘贴二维码内容或令牌" size="large" clearable @keyup.enter="goManual" />
        <el-button type="primary" plain class="mt-12" style="width: 100%" @click="goManual">查询出门条</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Html5Qrcode } from 'html5-qrcode'
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()
const scanning = ref(false)
const manual = ref('')
let reader = null

function parseToken(text) {
  const t = (text || '').trim()
  const m = t.match(/verify\/([A-Za-z0-9]+)/) || t.match(/pass\/([A-Za-z0-9]+)/)
  return m ? m[1] : /^[A-Za-z0-9]{16,64}$/.test(t) ? t : null
}
async function start() {
  try {
    reader = new Html5Qrcode('ght-qr-reader')
    scanning.value = true
    await reader.start({ facingMode: 'environment' }, { fps: 10, qrbox: 230 }, (text) => {
      const token = parseToken(text)
      if (token) {
        stop()
        router.push(`/gate/verify/${token}`)
      }
    })
  } catch (e) {
    scanning.value = false
    ElMessage.error('无法打开摄像头，请检查浏览器权限（需 HTTPS 或 localhost）')
  }
}
async function stop() {
  scanning.value = false
  try {
    if (reader) await reader.stop()
  } catch (e) {
    /* ignore */
  }
}
function goManual() {
  const token = parseToken(manual.value)
  if (!token) return ElMessage.warning('无法识别的出门条内容')
  router.push(`/gate/verify/${token}`)
}
onBeforeUnmount(stop)
</script>

<style scoped lang="scss">
.scan-layout { display: grid; grid-template-columns: minmax(320px, 520px) minmax(280px, 380px); gap: 18px; }
.scanner { padding: 20px; text-align: center; }
.frame { position: relative; aspect-ratio: 1; border-radius: 18px; overflow: hidden; background: #0b1847; }
.reader { width: 100%; height: 100%; :deep(video) { object-fit: cover; } }
.placeholder { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; background: linear-gradient(160deg, #f3f8ff, #e7f0fc); color: var(--ght-ink-3); }
.laser { animation: laser 2s ease-in-out infinite; }
@keyframes laser { 0%, 100% { transform: translateY(-24px); } 50% { transform: translateY(24px); } }
.btns { margin-top: 16px; }
.tip { font-size: 12px; color: var(--ght-ink-3); margin-top: 12px; }
.manual { padding: 20px; align-self: start; p { font-size: 13px; } }
@media (max-width: 900px) { .scan-layout { grid-template-columns: 1fr; } }
</style>
