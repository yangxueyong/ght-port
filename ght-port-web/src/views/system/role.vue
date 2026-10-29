<template>
  <div class="ght-page">
    <PageHeader title="角色管理" icon="Avatar" subtitle="内置角色：租户管理员、港口调度员、承运商、司机、门卫；可为角色分配菜单与按钮权限">
      <el-button v-perm="'sys:role:edit'" type="primary" icon="Plus" @click="open()">新增角色</el-button>
    </PageHeader>
    <div class="role-layout">
      <div class="role-list">
        <div v-for="r in list" :key="r.id" class="role-item" :class="{ active: current?.id === r.id }" @click="select(r)">
          <div class="flex-between">
            <b>{{ r.roleName }}</b>
            <el-tag v-if="r.builtin === 1" size="small" effect="plain" round>内置</el-tag>
          </div>
          <div class="muted small">{{ r.roleCode }}</div>
          <div class="muted small ellipsis">{{ r.remark || '—' }}</div>
          <div class="ops" v-perm="'sys:role:edit'">
            <el-button link size="small" type="primary" @click.stop="open(r)">编辑</el-button>
            <el-button v-if="r.builtin !== 1" link size="small" type="danger" @click.stop="remove(r)">删除</el-button>
          </div>
        </div>
      </div>
      <div class="ght-card perm-panel">
        <template v-if="current">
          <div class="flex-between">
            <div class="section-title">{{ current.roleName }} · 菜单与按钮权限</div>
            <div>
              <el-button size="small" @click="expandAll">全部展开</el-button>
              <el-button v-perm="'sys:role:edit'" size="small" type="primary" icon="Check" @click="saveMenus">保存权限</el-button>
            </div>
          </div>
          <el-tree ref="treeRef" class="mt-16" :data="menus" node-key="id" show-checkbox :props="{ label: 'menuName', children: 'children' }" :default-expand-all="true">
            <template #default="{ data }">
              <span class="node"><el-icon v-if="data.icon"><component :is="data.icon" /></el-icon>{{ data.menuName }}
                <em v-if="data.menuType === 'BUTTON'">按钮</em><code v-if="data.perms">{{ data.perms }}</code></span>
            </template>
          </el-tree>
        </template>
        <div v-else class="muted">请选择左侧角色</div>
      </div>
    </div>
    <el-dialog v-model="visible" :title="form.id ? '编辑角色' : '新增角色'" width="440px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色编码" prop="roleCode"><el-input v-model="form.roleCode" :disabled="form.builtin === 1" /></el-form-item>
        <el-form-item label="角色名称" prop="roleName"><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
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
import { nextTick, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { rolePage, roleSave, roleDelete, roleMenuIds, roleAssignMenus, menuTree } from '@/api/system'

const list = ref([])
const menus = ref([])
const current = ref(null)
const treeRef = ref()
const visible = ref(false)
const form = ref({})
const formRef = ref()
const rules = { roleCode: [{ required: true, message: '请输入角色编码' }], roleName: [{ required: true, message: '请输入角色名称' }] }

async function load() {
  const res = await rolePage({ pageNum: 1, pageSize: 100 })
  list.value = res.records
  if (!current.value && list.value.length) select(list.value[0])
}
async function select(r) {
  current.value = r
  const ids = await roleMenuIds(r.id)
  await nextTick()
  const leafIds = ids.filter((id) => {
    const node = treeRef.value?.getNode(id)
    return node && node.isLeaf
  })
  treeRef.value?.setCheckedKeys(leafIds)
}
function expandAll() {
  Object.values(treeRef.value.store.nodesMap).forEach((n) => (n.expanded = true))
}
async function saveMenus() {
  const ids = [...treeRef.value.getCheckedKeys(), ...treeRef.value.getHalfCheckedKeys()]
  await roleAssignMenus({ roleId: current.value.id, menuIds: ids })
  ElMessage.success('权限已保存，用户重新进入页面后生效')
}
function open(r) {
  form.value = r ? { id: r.id, roleCode: r.roleCode, roleName: r.roleName, sort: r.sort, remark: r.remark, builtin: r.builtin } : { sort: 10 }
  visible.value = true
}
async function submit() {
  await formRef.value.validate()
  const { id, roleCode, roleName, sort, remark } = form.value
  await roleSave({ id, roleCode, roleName, sort, remark })
  ElMessage.success('已保存')
  visible.value = false
  load()
}
async function remove(r) {
  await ElMessageBox.confirm(`确定删除角色【${r.roleName}】吗？`, '删除确认', { type: 'warning' })
  await roleDelete(r.id)
  ElMessage.success('已删除')
  if (current.value?.id === r.id) current.value = null
  load()
}
onMounted(async () => {
  menus.value = await menuTree()
  load()
})
</script>

<style scoped lang="scss">
.role-layout { display: grid; grid-template-columns: 280px minmax(0, 1fr); gap: 16px; }
.role-list { display: flex; flex-direction: column; gap: 10px; }
.role-item { background: #fff; border: 1px solid var(--ght-line); border-radius: 14px; padding: 12px 14px; cursor: pointer; display: flex; flex-direction: column; gap: 4px;
  &.active { border-color: var(--ght-blue); box-shadow: 0 10px 22px -14px rgba(31, 122, 224, 0.8); } }
.small { font-size: 12px; }
.ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ops { display: flex; gap: 8px; }
.perm-panel { padding: 16px 18px; min-height: 400px; }
.node { display: flex; align-items: center; gap: 6px; font-size: 13px;
  em { font-style: normal; font-size: 11px; padding: 0 6px; border-radius: 4px; background: #fff4de; color: #b76e00; }
  code { font-size: 11px; color: var(--ght-ink-3); background: #f4f6fa; padding: 0 6px; border-radius: 4px; } }
</style>
