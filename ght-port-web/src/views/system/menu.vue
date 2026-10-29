<template>
  <div class="ght-page">
    <PageHeader title="菜单管理" icon="Menu" subtitle="维护系统菜单、按钮与权限标识（权限标识用于后端接口鉴权）">
      <el-button v-perm="'sys:menu:edit'" type="primary" icon="Plus" @click="open()">新增菜单</el-button>
    </PageHeader>
    <div class="ght-card" style="padding: 6px">
      <el-table :data="tree" row-key="id" default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column label="菜单名称" min-width="200">
          <template #default="{ row }"><span class="flex-center gap-8"><el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>{{ row.menuName }}</span></template>
        </el-table-column>
        <el-table-column label="类型" width="90"><template #default="{ row }"><el-tag size="small" :type="typeTag[row.menuType]">{{ typeLabel[row.menuType] }}</el-tag></template></el-table-column>
        <el-table-column prop="path" label="路由" min-width="140" />
        <el-table-column prop="component" label="组件" min-width="150" />
        <el-table-column prop="perms" label="权限标识" min-width="170" />
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <span v-perm="'sys:menu:edit'">
              <el-button v-if="row.menuType !== 'BUTTON'" link type="primary" @click="open(null, row)">新增下级</el-button>
              <el-button link @click="open(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="visible" :title="form.id ? '编辑菜单' : '新增菜单'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="84px">
        <el-form-item label="上级菜单">
          <el-tree-select v-model="form.parentId" :data="parentOptions" check-strictly :props="{ label: 'menuName', value: 'id', children: 'children' }" placeholder="顶级菜单" clearable style="width: 100%" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType"><el-radio-button value="DIR">目录</el-radio-button><el-radio-button value="MENU">菜单</el-radio-button><el-radio-button value="BUTTON">按钮</el-radio-button></el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName"><el-input v-model="form.menuName" /></el-form-item>
        <el-form-item v-if="form.menuType !== 'BUTTON'" label="路由地址"><el-input v-model="form.path" placeholder="/carrier" /></el-form-item>
        <el-form-item v-if="form.menuType === 'MENU'" label="组件路径"><el-input v-model="form.component" placeholder="carrier/index（相对 src/views）" /></el-form-item>
        <el-form-item v-if="form.menuType !== 'DIR'" label="权限标识"><el-input v-model="form.perms" placeholder="carrier:list" /></el-form-item>
        <el-form-item v-if="form.menuType !== 'BUTTON'" label="图标"><el-input v-model="form.icon" placeholder="Element Plus 图标名，如 Van" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item v-if="form.menuType !== 'BUTTON'" label="是否显示"><el-switch v-model="form.visible" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { menuTree, menuSave, menuDelete } from '@/api/system'

const tree = ref([])
const visible = ref(false)
const form = ref({})
const formRef = ref()
const typeLabel = { DIR: '目录', MENU: '菜单', BUTTON: '按钮' }
const typeTag = { DIR: 'info', MENU: 'primary', BUTTON: 'warning' }
const rules = { menuName: [{ required: true, message: '请输入菜单名称' }], menuType: [{ required: true, message: '请选择类型' }] }
const strip = (list) => list.filter((m) => m.menuType !== 'BUTTON').map((m) => ({ ...m, children: strip(m.children || []) }))
const parentOptions = computed(() => strip(tree.value))
async function load() {
  tree.value = await menuTree()
}
function open(row, parent) {
  form.value = row
    ? { id: row.id, parentId: row.parentId === '0' ? null : row.parentId, menuName: row.menuName, menuType: row.menuType, path: row.path, component: row.component, perms: row.perms, icon: row.icon, sort: row.sort, visible: row.visible }
    : { parentId: parent?.id || null, menuType: parent ? (parent.menuType === 'MENU' ? 'BUTTON' : 'MENU') : 'MENU', sort: 0, visible: 1 }
  visible.value = true
}
async function submit() {
  await formRef.value.validate()
  await menuSave(form.value)
  ElMessage.success('已保存')
  visible.value = false
  load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除菜单【${row.menuName}】吗？`, '删除确认', { type: 'warning' })
  await menuDelete(row.id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>
