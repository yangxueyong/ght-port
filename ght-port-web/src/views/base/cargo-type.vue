<template>
  <div class="ght-page">
    <PageHeader title="货物类型" icon="Box" subtitle="泊位到货时可下拉搜索选择的货物类型">
      <el-input v-model="keyword" placeholder="搜索类型" prefix-icon="Search" clearable style="width: 200px" @change="load" />
      <ViewToggle v-model="view" storage-key="cargo-type" />
      <el-button v-perm="'cargo:type:edit'" type="primary" icon="Plus" @click="open()">新增类型</el-button>
    </PageHeader>
    <div v-if="view === 'card'" class="type-grid">
      <div v-for="t in list" :key="t.id" class="type-card hover-lift" :style="{ '--c': t.color || '#1f7ae0' }">
        <div class="swatch"><span>{{ t.typeName.slice(0, 1) }}</span></div>
        <div class="info">
          <div class="name">{{ t.typeName }}</div>
          <div class="muted small">{{ t.typeCode || '—' }} · 排序 {{ t.sort ?? 0 }}</div>
        </div>
        <div class="ops">
          <el-button v-perm="'cargo:type:edit'" link type="primary" @click="open(t)">编辑</el-button>
          <el-button v-perm="'cargo:type:edit'" link type="danger" @click="remove(t)">删除</el-button>
        </div>
      </div>
    </div>
    <div v-else class="ght-card" style="padding: 6px">
      <el-table :data="list">
        <el-table-column label="颜色" width="80"><template #default="{ row }"><span class="dot" :style="{ background: row.color }" /></template></el-table-column>
        <el-table-column prop="typeName" label="类型名称" />
        <el-table-column prop="typeCode" label="编码" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button v-perm="'cargo:type:edit'" link type="primary" @click="open(row)">编辑</el-button>
            <el-button v-perm="'cargo:type:edit'" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <EmptyState v-if="!list.length" />
    <el-dialog v-model="visible" :title="form.id ? '编辑货物类型' : '新增货物类型'" width="440px">
      <el-form ref="formRef" :model="form" :rules="{ typeName: [{ required: true, message: '请输入类型名称' }] }" label-width="80px">
        <el-form-item label="类型名称" prop="typeName"><el-input v-model="form.typeName" /></el-form-item>
        <el-form-item label="类型编码"><el-input v-model="form.typeCode" /></el-form-item>
        <el-form-item label="颜色"><el-color-picker v-model="form.color" :predefine="PALETTE_COLORS" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import ViewToggle from '@/components/common/ViewToggle.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { cargoTypeList, cargoTypeSave, cargoTypeDelete } from '@/api/berth'
import { PALETTE_COLORS } from '@/utils/dict'

const view = ref('card')
const list = ref([])
const keyword = ref('')
const visible = ref(false)
const form = ref({})
const formRef = ref()
async function load() {
  list.value = await cargoTypeList(keyword.value)
}
function open(t) {
  form.value = t ? { id: t.id, typeName: t.typeName, typeCode: t.typeCode, color: t.color, sort: t.sort } : { color: PALETTE_COLORS[0], sort: 0 }
  visible.value = true
}
async function submit() {
  await formRef.value.validate()
  await cargoTypeSave(form.value)
  ElMessage.success('已保存')
  visible.value = false
  load()
}
async function remove(t) {
  await ElMessageBox.confirm(`确定删除货物类型【${t.typeName}】吗？`, '删除确认', { type: 'warning' })
  await cargoTypeDelete(t.id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

<style scoped lang="scss">
.type-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 14px; }
.type-card { display: flex; align-items: center; gap: 12px; padding: 14px; background: #fff; border: 1px solid var(--ght-line); border-radius: 16px; position: relative; overflow: hidden;
  &::after { content: ''; position: absolute; right: -24px; top: -24px; width: 80px; height: 80px; border-radius: 50%; background: color-mix(in srgb, var(--c) 12%, transparent); } }
.swatch { width: 48px; height: 48px; border-radius: 14px; display: grid; place-items: center; background: linear-gradient(135deg, color-mix(in srgb, var(--c) 70%, #fff), var(--c)); color: #fff; font-weight: 800; font-size: 18px; box-shadow: 0 8px 16px -8px var(--c); }
.info { flex: 1; }
.name { font-weight: 700; }
.small { font-size: 12px; }
.ops { display: flex; flex-direction: column; position: relative; z-index: 1; .el-button { margin: 0; } }
.dot { display: inline-block; width: 16px; height: 16px; border-radius: 5px; }
</style>
