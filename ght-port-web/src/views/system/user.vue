<template>
  <div class="ght-page">
    <PageHeader title="用户管理" icon="User" subtitle="管理本港口的系统账号；新增承运商、车辆时会自动开通承运商、司机账号">
      <el-select v-model="query.userType" placeholder="用户类型" clearable style="width: 130px" @change="search">
        <el-option v-for="t in USER_TYPES" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="账号/昵称/手机号" prefix-icon="Search" clearable style="width: 200px" @change="search" />
      <ViewToggle v-model="view" storage-key="user" />
      <el-button v-perm="'sys:user:edit'" type="primary" icon="Plus" @click="open()">新增用户</el-button>
    </PageHeader>
    <div v-loading="loading">
      <div v-if="view === 'card'" class="card-grid">
        <div v-for="u in list" :key="u.id" class="u-card hover-lift">
          <div class="u-head">
            <div class="avatar" :class="u.userType">{{ (u.nickname || u.username).slice(0, 1) }}</div>
            <div class="flex-1">
              <div class="name">{{ u.nickname }} <span class="type">{{ labelOf(USER_TYPES, u.userType) }}</span></div>
              <div class="muted small">{{ u.username }} · {{ u.phone || '—' }}</div>
            </div>
            <StatusTag :dict="ENABLE_STATUS" :value="u.status" />
          </div>
          <div class="roles"><span v-for="r in u.roleNames" :key="r">{{ r }}</span><span v-if="!u.roleNames?.length" class="none">未分配角色</span></div>
          <div class="muted small">最近登录：{{ u.lastLoginTime ? fmtTime(u.lastLoginTime) : '从未登录' }}</div>
          <div class="ops" v-perm="'sys:user:edit'">
            <el-button size="small" @click="open(u)">编辑</el-button>
            <el-button size="small" @click="resetPwd(u)">重置密码</el-button>
            <el-button size="small" text :type="u.status === 'ENABLED' ? 'warning' : 'success'" @click="toggle(u)">{{ u.status === 'ENABLED' ? '停用' : '启用' }}</el-button>
            <el-button size="small" text type="danger" @click="remove(u)">删除</el-button>
          </div>
        </div>
      </div>
      <div v-else class="ght-card" style="padding: 6px">
        <el-table :data="list">
          <el-table-column prop="username" label="账号" width="140" />
          <el-table-column prop="nickname" label="昵称" width="120" />
          <el-table-column label="类型" width="100"><template #default="{ row }">{{ labelOf(USER_TYPES, row.userType) }}</template></el-table-column>
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column label="角色" min-width="160"><template #default="{ row }">{{ (row.roleNames || []).join('、') }}</template></el-table-column>
          <el-table-column label="状态" width="90"><template #default="{ row }"><StatusTag :dict="ENABLE_STATUS" :value="row.status" /></template></el-table-column>
          <el-table-column label="最近登录" width="150"><template #default="{ row }">{{ fmtTime(row.lastLoginTime) }}</template></el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <span v-perm="'sys:user:edit'">
                <el-button link type="primary" @click="open(row)">编辑</el-button>
                <el-button link @click="resetPwd(row)">重置密码</el-button>
                <el-button link @click="toggle(row)">{{ row.status === 'ENABLED' ? '停用' : '启用' }}</el-button>
                <el-button link type="danger" @click="remove(row)">删除</el-button>
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-pagination v-if="total > query.pageSize" class="pager" background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="load" />
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑用户' : '新增用户'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="账号" prop="username"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="昵称" prop="nickname"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="form.userType" style="width: 100%"><el-option v-for="t in USER_TYPES" :key="t.value" :label="t.label" :value="t.value" /></el-select>
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码"><el-input v-model="form.password" placeholder="不填则为 123456" show-password /></el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="form.roleIds"><el-checkbox v-for="r in roles" :key="r.id" :value="r.id">{{ r.roleName }}</el-checkbox></el-checkbox-group>
        </el-form-item>
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
import ViewToggle from '@/components/common/ViewToggle.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { USER_TYPES, ENABLE_STATUS, labelOf } from '@/utils/dict'
import { fmtTime } from '@/utils/format'
import { userPage, userSave, userDelete, userResetPwd, userStatus, roleList } from '@/api/system'

const view = ref('card')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const roles = ref([])
const query = reactive({ pageNum: 1, pageSize: 12, keyword: '', userType: '' })
const visible = ref(false)
const form = ref({})
const formRef = ref()
const rules = {
  username: [{ required: true, message: '请输入账号' }],
  nickname: [{ required: true, message: '请输入昵称' }],
  userType: [{ required: true, message: '请选择用户类型' }]
}
async function load() {
  loading.value = true
  try {
    const res = await userPage(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}
function search() {
  query.pageNum = 1
  load()
}
function open(u) {
  form.value = u ? { id: u.id, username: u.username, nickname: u.nickname, phone: u.phone, userType: u.userType, roleIds: [...(u.roleIds || [])] } : { userType: 'STAFF', roleIds: [] }
  visible.value = true
}
async function submit() {
  await formRef.value.validate()
  await userSave(form.value)
  ElMessage.success('已保存')
  visible.value = false
  load()
}
async function resetPwd(u) {
  const { value } = await ElMessageBox.prompt(`为【${u.nickname}】设置新密码（留空则重置为 123456）`, '重置密码', { inputType: 'password', inputPlaceholder: '新密码' })
  await userResetPwd({ id: u.id, password: value })
  ElMessage.success('密码已重置')
}
async function toggle(u) {
  await userStatus({ id: u.id, status: u.status === 'ENABLED' ? 'DISABLED' : 'ENABLED' })
  ElMessage.success('已更新')
  load()
}
async function remove(u) {
  await ElMessageBox.confirm(`确定删除用户【${u.nickname}】吗？`, '删除确认', { type: 'warning' })
  await userDelete(u.id)
  ElMessage.success('已删除')
  load()
}
onMounted(async () => {
  load()
  roles.value = await roleList()
})
</script>

<style scoped lang="scss">
.u-card { background: #fff; border: 1px solid var(--ght-line); border-radius: 16px; padding: 16px; display: flex; flex-direction: column; gap: 10px; }
.u-head { display: flex; align-items: center; gap: 12px; }
.avatar { width: 44px; height: 44px; border-radius: 14px; display: grid; place-items: center; color: #fff; font-weight: 700; font-size: 18px; background: linear-gradient(135deg, #1f7ae0, #182d86);
  &.CARRIER { background: linear-gradient(135deg, #f59f00, #d9480f); } &.DRIVER { background: linear-gradient(135deg, #22c3e6, #0a64b0); } &.GUARD { background: linear-gradient(135deg, #12b886, #087f5b); } }
.flex-1 { flex: 1; min-width: 0; }
.name { font-weight: 700; }
.type { font-size: 11px; font-weight: 500; padding: 1px 6px; border-radius: 5px; background: #eef2f8; color: var(--ght-ink-2); margin-left: 4px; }
.small { font-size: 12px; }
.roles { display: flex; flex-wrap: wrap; gap: 6px; span { font-size: 12px; padding: 2px 8px; border-radius: 6px; background: #e8f1fd; color: var(--ght-blue); } .none { background: #f4f5f7; color: var(--ght-ink-3); } }
.ops { display: flex; gap: 4px; flex-wrap: wrap; .el-button { margin: 0; } }
.pager { margin-top: 18px; justify-content: flex-end; }
</style>
