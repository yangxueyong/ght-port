<template>
  <div class="ght-page port-page">
    <PageHeader title="港口管理" icon="MapLocation" subtitle="3D 数字孪生港口：拖入江河、陆地、泊位等元素搭建港区，可移动、缩放、旋转、删除任意元素，河道可弯曲、两岸均可设泊位">
      <el-button v-perm="'port:map:edit'" type="primary" icon="Plus" @click="openPortDialog()">新建港口</el-button>
    </PageHeader>

    <div class="editor-wrap" v-loading="loading">
      <PortEditor v-if="detail" ref="editorRef" :detail="detail" :readonly="!canEdit" @saved="reloadAll" @reload="loadDetail">
        <template #title>
          <el-popover v-model:visible="switcherVisible" placement="bottom-start" :width="300" trigger="click" popper-class="port-switch-pop">
            <template #reference>
              <div class="port-switch">
                <span class="ps-name">{{ current?.portName }}</span>
                <span class="ps-meta"><b class="num">{{ current?.berthTotal || 0 }}</b> 泊位 · <b class="num w">{{ current?.berthWorking || 0 }}</b> 作业中</span>
                <el-icon class="ps-arrow"><ArrowDown /></el-icon>
              </div>
            </template>
            <div class="ps-title">切换港口（{{ ports.length }}）</div>
            <div class="ps-list">
              <div v-for="p in ports" :key="p.id" class="ps-item" :class="{ active: p.id === currentId }" @click="select(p.id)">
                <div class="pi-head">
                  <div class="pi-name">{{ p.portName }}</div>
                  <span v-if="canEdit" class="pi-ops" @click.stop>
                    <el-button link size="small" icon="Edit" @click="openPortDialog(p)" />
                    <el-button link size="small" type="danger" icon="Delete" @click="removePort(p)" />
                  </span>
                </div>
                <div class="pi-addr">{{ p.address || '未填写地址' }}</div>
                <div class="pi-bar">
                  <span :style="{ flex: p.berthIdle || 0, background: 'var(--ght-idle)' }" />
                  <span :style="{ flex: p.berthWorking || 0, background: 'var(--ght-working)' }" />
                  <span :style="{ flex: p.berthMaintenance || 0, background: 'var(--ght-maint)' }" />
                  <span v-if="!p.berthTotal" :style="{ flex: 1, background: '#e6ebf3' }" />
                </div>
                <div class="pi-stats">
                  <span><b class="num">{{ p.berthTotal }}</b> 泊位</span>
                  <span class="w"><b class="num">{{ p.berthWorking }}</b> 作业中</span>
                </div>
              </div>
            </div>
          </el-popover>
        </template>
      </PortEditor>
      <EmptyState v-else-if="!loading" :text="ports.length ? '请选择港口' : '还没有港口，点击右上角“新建港口”开始搭建'" />
    </div>

    <el-dialog v-model="portDialog.visible" :title="portDialog.form.id ? '编辑港口' : '新建港口'" width="480px">
      <el-form ref="portFormRef" :model="portDialog.form" :rules="portRules" label-width="84px">
        <el-form-item label="港口名称" prop="portName"><el-input v-model="portDialog.form.portName" placeholder="如：涪陵龙头港" /></el-form-item>
        <el-form-item label="港口编码"><el-input v-model="portDialog.form.portCode" placeholder="如：FLLT" /></el-form-item>
        <el-form-item label="港口地址"><el-input v-model="portDialog.form.address" /></el-form-item>
        <el-form-item label="港口简介"><el-input v-model="portDialog.form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="portDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitPort">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import PortEditor from './components/PortEditor.vue'
import { portList, portDetail, portSave, portDelete } from '@/api/port'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const canEdit = computed(() => store.hasPerm('port:map:edit'))
const ports = ref([])
const currentId = ref(null)
const detail = ref(null)
const loading = ref(false)
const editorRef = ref()
const switcherVisible = ref(false)
const current = computed(() => ports.value.find((p) => p.id === currentId.value))

async function loadPorts() {
  ports.value = await portList()
  if (!currentId.value && ports.value.length) currentId.value = ports.value[0].id
}
async function loadDetail() {
  if (!currentId.value) {
    detail.value = null
    return
  }
  loading.value = true
  try {
    detail.value = await portDetail(currentId.value)
  } finally {
    loading.value = false
  }
}
async function reloadAll() {
  await loadPorts()
  await loadDetail()
}
async function select(id) {
  switcherVisible.value = false
  if (id === currentId.value) return
  if (editorRef.value?.isDirty()) {
    try {
      await ElMessageBox.confirm('当前港口有未保存的修改，切换后将丢失，是否继续？', '提示', { type: 'warning' })
    } catch (e) {
      return
    }
  }
  currentId.value = id
  loadDetail()
}

const portFormRef = ref()
const portDialog = reactive({ visible: false, form: {} })
const portRules = { portName: [{ required: true, message: '请输入港口名称' }] }
function openPortDialog(p) {
  switcherVisible.value = false
  portDialog.form = p ? { id: p.id, portName: p.portName, portCode: p.portCode, address: p.address, description: p.description } : {}
  portDialog.visible = true
}
async function submitPort() {
  await portFormRef.value.validate()
  const id = await portSave(portDialog.form)
  ElMessage.success('已保存')
  portDialog.visible = false
  if (!portDialog.form.id) currentId.value = id
  reloadAll()
}
async function removePort(p) {
  switcherVisible.value = false
  await ElMessageBox.confirm(`确定删除港口【${p.portName}】吗？其 3D 场景与泊位将一并删除。`, '删除确认', { type: 'warning' })
  await portDelete(p.id)
  ElMessage.success('已删除')
  if (currentId.value === p.id) currentId.value = null
  reloadAll()
}

onBeforeRouteLeave(async () => {
  if (editorRef.value?.isDirty()) {
    try {
      await ElMessageBox.confirm('港口有未保存的修改，确定离开吗？', '提示', { type: 'warning' })
    } catch (e) {
      return false
    }
  }
  return true
})

onMounted(reloadAll)
</script>

<style scoped lang="scss">
.port-page { height: 100%; display: flex; flex-direction: column; }
.editor-wrap { flex: 1; min-width: 0; min-height: 560px; height: calc(100vh - 190px); }
.port-switch {
  display: flex; align-items: center; gap: 10px; padding: 5px 10px 5px 12px; margin-right: 6px; border-radius: 10px; cursor: pointer;
  border: 1px solid #dbe5f4; background: #fff; transition: all 0.2s;
  &:hover { border-color: var(--ght-blue); box-shadow: 0 6px 16px -10px rgba(31, 122, 224, 0.8); }
}
.ps-name { font-weight: 700; font-size: 15px; letter-spacing: 0.5px; white-space: nowrap; }
.ps-meta { font-size: 12px; color: var(--ght-ink-3); white-space: nowrap; b { color: var(--ght-ink); } .w { color: var(--ght-working); } }
.ps-arrow { color: var(--ght-ink-3); }
</style>

<style lang="scss">
.port-switch-pop {
  .ps-title { font-size: 12px; color: var(--ght-ink-3); margin-bottom: 8px; }
  .ps-list { display: flex; flex-direction: column; gap: 8px; max-height: 360px; overflow-y: auto; }
  .ps-item {
    padding: 10px 12px; border-radius: 12px; border: 1px solid var(--ght-line); cursor: pointer; transition: all 0.2s;
    &:hover { border-color: #bcd5f5; }
    &.active { border-color: var(--ght-blue); background: linear-gradient(180deg, #fff, #f3f8ff); }
  }
  .pi-head { display: flex; justify-content: space-between; align-items: center; }
  .pi-name { font-weight: 700; }
  .pi-ops .el-button { margin-left: 4px; }
  .pi-addr { font-size: 12px; color: var(--ght-ink-3); margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .pi-bar { display: flex; height: 5px; border-radius: 5px; overflow: hidden; margin-top: 8px; gap: 2px; span { min-width: 0; } }
  .pi-stats { display: flex; justify-content: space-between; font-size: 12px; color: var(--ght-ink-3); margin-top: 6px; b { color: var(--ght-ink); } .w b { color: var(--ght-working); } }
}
</style>
