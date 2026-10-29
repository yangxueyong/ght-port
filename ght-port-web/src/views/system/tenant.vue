<template>
  <div class="ght-page">
    <PageHeader title="租户管理" icon="OfficeBuilding" subtitle="将系统开通给其他港口使用，不同租户数据完全隔离；新建租户会自动初始化内置角色与管理员账号">
      <el-input v-model="query.keyword" placeholder="租户名称/编码" prefix-icon="Search" clearable style="width: 200px" @change="load" />
      <el-button v-perm="'sys:tenant:edit'" type="primary" icon="Plus" @click="open()">开通租户</el-button>
    </PageHeader>
    <div class="card-grid">
      <div v-for="t in list" :key="t.id" class="t-card hover-lift" :class="t.status">
        <div class="t-top">
          <div class="t-icon"><el-icon><OfficeBuilding /></el-icon></div>
          <div class="flex-1">
            <div class="t-name">{{ t.tenantName }}</div>
            <div class="muted small">登录编码：<code>{{ t.tenantCode }}</code></div>
          </div>
          <StatusTag :dict="ENABLE_STATUS" :value="t.status" />
        </div>
        <div class="t-info muted small">
          <span>联系人：{{ t.contactName || '—' }} {{ t.contactPhone || '' }}</span>
          <span>到期：{{ t.expireTime ? fmtTime(t.expireTime, 'YYYY-MM-DD') : '长期' }}</span>
        </div>
        <div class="ops" v-if="t.id !== '000000'" v-perm="'sys:tenant:edit'">
          <el-button size="small" @click="open(t)">编辑</el-button>
          <el-button size="small" text :type="t.status === 'ENABLED' ? 'warning' : 'success'" @click="toggle(t)">{{ t.status === 'ENABLED' ? '停用' : '启用' }}</el-button>
          <el-button size="small" text type="danger" @click="remove(t)">删除</el-button>
        </div>
        <div v-else class="muted small platform">平台租户</div>
      </div>
    </div>
    <el-dialog v-model="visible" :title="form.id ? '编辑租户' : '开通租户'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="租户名称" prop="tenantName"><el-input v-model="form.tenantName" placeholder="如：万州新田港" /></el-form-item>
        <el-form-item label="登录编码" prop="tenantCode"><el-input v-model="form.tenantCode" placeholder="字母数字，如 wzxt" /></el-form-item>
        <el-form-item v-if="!form.id" label="管理员账号" prop="adminUsername"><el-input v-model="form.adminUsername" placeholder="默认密码 123456" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
        <el-form-item label="到期时间"><el-date-picker v-model="form.expireTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="不填为长期" style="width: 100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { ENABLE_STATUS } from '@/utils/dict'
import { fmtTime } from '@/utils/format'
import { tenantPage, tenantSave, tenantStatus, tenantDelete } from '@/api/system'

const list = ref([])
const query = reactive({ pageNum: 1, pageSize: 100, keyword: '' })
const visible = ref(false)
const form = ref({})
const formRef = ref()
const rules = {
  tenantName: [{ required: true, message: '请输入租户名称' }],
  tenantCode: [{ required: true, pattern: /^[a-zA-Z0-9_]{2,32}$/, message: '编码为2-32位字母、数字或下划线' }],
  adminUsername: [{ required: true, message: '请输入管理员账号' }]
}
async function load() {
  list.value = (await tenantPage(query)).records
}
function open(t) {
  form.value = t ? { id: t.id, tenantName: t.tenantName, tenantCode: t.tenantCode, contactName: t.contactName, contactPhone: t.contactPhone, expireTime: t.expireTime, remark: t.remark } : { adminUsername: 'admin' }
  visible.value = true
}
async function submit() {
  await formRef.value.validate()
  await tenantSave(form.value)
  ElMessage.success(form.value.id ? '已保存' : `开通成功，管理员账号 ${form.value.adminUsername} / 123456`)
  visible.value = false
  load()
}
async function toggle(t) {
  await tenantStatus({ id: t.id, status: t.status === 'ENABLED' ? 'DISABLED' : 'ENABLED' })
  load()
}
async function remove(t) {
  await ElMessageBox.confirm(`确定删除租户【${t.tenantName}】吗？该租户将无法登录。`, '删除确认', { type: 'warning' })
  await tenantDelete(t.id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

<style scoped lang="scss">
.t-card { background: #fff; border: 1px solid var(--ght-line); border-radius: 16px; padding: 16px; display: flex; flex-direction: column; gap: 12px; &.DISABLED { opacity: 0.7; } }
.t-top { display: flex; align-items: center; gap: 12px; }
.t-icon { width: 46px; height: 46px; border-radius: 14px; display: grid; place-items: center; font-size: 22px; color: #fff; background: linear-gradient(135deg, #1f7ae0, #182d86); }
.flex-1 { flex: 1; }
.t-name { font-weight: 700; font-size: 16px; }
.small { font-size: 12px; }
code { background: #eef2f8; padding: 1px 6px; border-radius: 4px; color: var(--ght-navy); }
.t-info { display: flex; justify-content: space-between; }
.ops { display: flex; gap: 6px; .el-button { margin: 0; } }
.platform { text-align: right; }
</style>
