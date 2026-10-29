<template>
  <div class="ticket" :class="{ compact, verified: pass.status === 'VERIFIED' }">
    <div class="t-head">
      <div class="brand">
        <img src="@/assets/logo.png" alt="" />
        <div>
          <div class="t-title">电子出门条</div>
          <div class="t-port">{{ pass.portName }} · {{ pass.berthName }}</div>
        </div>
      </div>
      <div class="pass-no num">{{ pass.passNo }}</div>
    </div>

    <div class="t-truck">
      <TruckSvg :type="pass.vehicleType" :length="truckLen" color="#1f7ae0" :moving="pass.status !== 'VERIFIED'" :width="compact ? 170 : 210" />
      <div class="plate">{{ pass.plateNo }}</div>
    </div>

    <div class="t-grid">
      <div><label>承运商</label><span>{{ pass.carrierName }}</span></div>
      <div><label>司机</label><span>{{ pass.driverName }} {{ maskPhone(pass.driverPhone) }}</span></div>
      <div class="full"><label>拉载货物</label><span>{{ pass.cargoName }}<em v-if="pass.cargoTypeName">{{ pass.cargoTypeName }}</em></span></div>
      <div class="load"><b class="num">{{ fmtNum(pass.loadTonnage, 3) }}</b><small>吨</small></div>
      <div class="load"><b class="num">{{ fmtNum(pass.loadVolume, 3) }}</b><small>方</small></div>
      <div class="load"><b class="num">{{ pass.loadPieces || 0 }}</b><small>件</small></div>
      <div class="full"><label>开具时间</label><span class="num">{{ fmtTime(pass.issueTime, 'YYYY-MM-DD HH:mm:ss') }}</span></div>
    </div>

    <div class="perforation"><i /><span /><i /></div>

    <div v-if="showQr" class="t-qr">
      <div class="qr-box" :class="{ dim: pass.status === 'VERIFIED' }">
        <img v-if="qr" :src="qr" alt="出门二维码" />
        <div v-if="pass.status === 'VERIFIED'" class="qr-void">已失效</div>
      </div>
      <div class="qr-tip">
        <template v-if="pass.status === 'VERIFIED'">已于 {{ fmtTime(pass.verifyTime, 'MM-DD HH:mm') }} 核销放行</template>
        <template v-else>请向门卫出示此二维码，扫码核销后放行</template>
      </div>
    </div>

    <div v-if="pass.status === 'VERIFIED'" class="stamp">已核销</div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import QRCode from 'qrcode'
import TruckSvg from '@/components/svg/TruckSvg.vue'
import { VEHICLE_TYPES } from '@/utils/dict'
import { fmtNum, fmtTime } from '@/utils/format'

const props = defineProps({ pass: { type: Object, required: true }, compact: Boolean, showQr: { type: Boolean, default: true } })
const qr = ref('')
const truckLen = computed(() => (VEHICLE_TYPES.find((v) => v.value === props.pass.vehicleType) || {}).length || 9.6)
const verifyUrl = computed(() => `${location.origin}${location.pathname}#/gate/verify/${props.pass.accessToken}`)
const maskPhone = (p) => (p && p.length === 11 ? p.slice(0, 3) + '****' + p.slice(7) : p || '')

watch(
  verifyUrl,
  async (url) => {
    qr.value = await QRCode.toDataURL(url, { margin: 1, width: 260, color: { dark: '#0b1847', light: '#ffffff' } })
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.ticket {
  position: relative;
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 18px 40px -18px rgba(11, 24, 71, 0.45);
  border: 1px solid #e3e9f3;
}
.t-head {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 18px;
  background: linear-gradient(120deg, #0f2366, #1f7ae0);
  color: #fff;
}
.brand { display: flex; align-items: center; gap: 10px; img { width: 34px; height: 34px; border-radius: 9px; background: #fff; padding: 3px; } }
.t-title { font-weight: 700; font-size: 16px; letter-spacing: 2px; }
.t-port { font-size: 12px; opacity: 0.8; margin-top: 2px; }
.pass-no { font-size: 12px; background: rgba(255, 255, 255, 0.15); padding: 4px 8px; border-radius: 6px; }
.t-truck {
  display: flex; align-items: flex-end; justify-content: space-between; gap: 10px;
  padding: 14px 18px 4px;
  background: linear-gradient(180deg, #f3f8ff, #fff);
}
.plate {
  font-size: 18px; font-weight: 800; letter-spacing: 1px; padding: 4px 10px; border-radius: 6px;
  background: #ffd43b; color: #1d2433; border: 2px solid #1d2433; white-space: nowrap;
}
.t-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px 12px; padding: 12px 18px 14px;
  > div { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
  > div:nth-child(1), > div:nth-child(2) { grid-column: span 3; flex-direction: row; justify-content: space-between; }
  .full { grid-column: span 3; }
  label { font-size: 12px; color: var(--ght-ink-3); }
  span { font-weight: 600; font-size: 14px; }
  em { font-style: normal; font-size: 11px; margin-left: 6px; padding: 1px 6px; border-radius: 4px; background: #e8f1fd; color: #1f7ae0; }
  .load { background: #f6f8fc; border-radius: 10px; padding: 8px 10px; flex-direction: row; align-items: baseline; gap: 4px;
    b { font-size: 20px; } small { color: var(--ght-ink-3); } }
}
.perforation {
  position: relative; display: flex; align-items: center; height: 18px;
  i { width: 18px; height: 18px; border-radius: 50%; background: var(--ght-bg); flex-shrink: 0; margin: 0 -9px; }
  span { flex: 1; border-top: 2px dashed #dbe3ef; margin: 0 14px; }
}
.t-qr { display: flex; flex-direction: column; align-items: center; padding: 10px 18px 20px; }
.qr-box { position: relative; width: 210px; height: 210px; padding: 8px; border-radius: 14px; border: 1px solid #e3e9f3; img { width: 100%; height: 100%; } &.dim img { opacity: 0.18; filter: grayscale(1); } }
.qr-void { position: absolute; inset: 0; display: grid; place-items: center; font-size: 22px; font-weight: 800; color: #e5484d; letter-spacing: 6px; }
.qr-tip { margin-top: 10px; font-size: 13px; color: var(--ght-ink-3); text-align: center; }
.stamp {
  position: absolute; right: 18px; top: 92px; transform: rotate(-16deg);
  border: 3px solid #e5484d; color: #e5484d; font-weight: 800; font-size: 20px; letter-spacing: 4px;
  padding: 4px 12px; border-radius: 10px; opacity: 0.85; background: rgba(255, 255, 255, 0.6);
}
.compact {
  .qr-box { width: 170px; height: 170px; }
  .t-grid { padding: 10px 16px; }
}
</style>
