<template>
  <div v-loading="loading" class="assign-panel">
    <template v-if="d">
      <!-- 到货：按承运商分组展示车辆 -->
      <template v-if="d.cargo.direction !== 'LOAD'">
        <EmptyState v-if="!groups.length" text="该批货物尚未分派车辆" />
        <div v-for="g in groups" :key="g.carrierId" class="group">
          <div class="g-head">
            <div class="avatar">{{ g.carrierName.slice(0, 2) }}</div>
            <div class="flex-1">
              <div class="g-name">{{ g.carrierName }}</div>
              <div class="muted small">{{ g.list.length }} 辆车 · 已运 {{ fmtNum(g.tonnage, 3) }} 吨</div>
            </div>
          </div>
          <div v-for="v in g.list" :key="v.id" class="row">
            <TruckSvg :type="v.vehicleType" :length="v.vehicleLength" :color="v.bodyColor || '#1f7ae0'" :moving="v.trip?.status === 'IN_TRANSIT'" :width="72" />
            <div class="plate">{{ v.plateNo }}</div>
            <div class="muted small one-line">{{ v.driverName }} · {{ v.driverPhone }}</div>
            <div class="load num">{{ v.trip ? fmtNum(v.trip.loadTonnage, 3) + ' 吨' : '—' }}</div>
            <div class="muted small time">{{ v.trip ? fmtTime(v.trip.departTime, 'MM-DD HH:mm') : '' }}</div>
            <StatusTag v-if="v.trip" :dict="TRIP_STATUS" :value="v.trip.status" />
            <StatusTag v-else :dict="DISPATCH_STATUS" :value="v.status" />
          </div>
        </div>
      </template>
      <!-- 装货：装船船只 -->
      <template v-else>
        <EmptyState v-if="!d.shipLoads.length" text="该批货物尚未分派船只" />
        <div v-for="l in d.shipLoads" :key="l.id" class="row">
          <ShipSvg :type="l.shipType" :length="l.shipLength" :color="l.hullColor || '#1f6fd1'" :width="78" />
          <div class="plate">{{ l.shipName }}</div>
          <div class="muted small one-line">{{ l.companyName }}</div>
          <div class="load num">{{ l.status === 'LOADED' ? fmtNum(l.loadTonnage, 3) + ' 吨' : '—' }}</div>
          <div class="muted small time">{{ l.loadTime ? fmtTime(l.loadTime, 'MM-DD HH:mm') : '' }}</div>
          <StatusTag :dict="SHIP_LOAD_STATUS" :value="l.status" />
        </div>
      </template>
    </template>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import TruckSvg from '@/components/svg/TruckSvg.vue'
import ShipSvg from '@/components/svg/ShipSvg.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { DISPATCH_STATUS, TRIP_STATUS, SHIP_LOAD_STATUS } from '@/utils/dict'
import { fmtNum, fmtTime } from '@/utils/format'
import { cargoDetail } from '@/api/berth'

/** 货物的承运商与车辆（到货）或装船船只（装货） */
const props = defineProps({ cargoId: String, stamp: Number })
const d = ref(null)
const loading = ref(false)

const groups = computed(() => {
  const map = new Map()
  ;(d.value?.dispatches || []).forEach((v) => {
    if (!map.has(v.carrierId)) map.set(v.carrierId, { carrierId: v.carrierId, carrierName: v.carrierName || '-', list: [], tonnage: 0 })
    const g = map.get(v.carrierId)
    g.list.push(v)
    g.tonnage += Number(v.trip?.loadTonnage || 0)
  })
  return [...map.values()]
})

async function load() {
  if (!props.cargoId) return
  loading.value = true
  try {
    d.value = await cargoDetail(props.cargoId)
  } finally {
    loading.value = false
  }
}
watch(() => [props.cargoId, props.stamp], load, { immediate: true })
</script>

<style scoped lang="scss">
.assign-panel { min-height: 60px; }
.group + .group { margin-top: 10px; }
.g-head { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.avatar { width: 32px; height: 32px; border-radius: 10px; display: grid; place-items: center; color: #fff; font-weight: 700; font-size: 12px; background: linear-gradient(135deg, #1f7ae0, #182d86); }
.g-name { font-weight: 700; white-space: nowrap; }
.flex-1 { flex: 1; min-width: 0; }
.small { font-size: 12px; }
.row { display: grid; grid-template-columns: 84px 100px minmax(0, 1fr) 90px 80px auto; align-items: center; gap: 8px; padding: 6px 8px; border-radius: 10px; background: #f8fafd;
  & + .row { margin-top: 6px; } }
.plate { font-weight: 700; white-space: nowrap; }
.one-line { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.load { font-weight: 700; text-align: right; }
.time { text-align: right; }
</style>
