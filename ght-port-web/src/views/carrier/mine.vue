<template>
  <div class="ght-page">
    <PageHeader title="我的车队" icon="Van" subtitle="管理本承运商车辆，查看运输中的车辆与货物，并在运输完成后进行确认" />
    <div class="ght-card wrap">
      <CarrierDetailPanel self-mode />
    </div>
  </div>
</template>

<script setup>
import PageHeader from '@/components/common/PageHeader.vue'
import CarrierDetailPanel from './components/CarrierDetailPanel.vue'
</script>

<style scoped>
.wrap { padding: 16px; }
</style>
