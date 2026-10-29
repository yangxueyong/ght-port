<template>
  <div class="ght-page">
    <PageHeader title="我的出门条" icon="Postcard" subtitle="查看本人驾驶车辆的出门条，点击可展示二维码给门卫核销" />
    <PassList :api="gatePassMine" storage-key="mypass" />
  </div>
</template>

<script setup>
import PageHeader from '@/components/common/PageHeader.vue'
import PassList from './components/PassList.vue'
import { gatePassMine } from '@/api/gatepass'
</script>
