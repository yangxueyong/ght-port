<template>
  <div class="ght-page">
    <PageHeader title="出门条记录" icon="Document" subtitle="每次“装载完成，立即出发”都会生成出门条，并以链接发送给司机；门卫扫码核销后放行" />
    <PassList :api="gatePassPage" storage-key="gatepass" />
  </div>
</template>

<script setup>
import PageHeader from '@/components/common/PageHeader.vue'
import PassList from './components/PassList.vue'
import { gatePassPage } from '@/api/gatepass'
</script>
