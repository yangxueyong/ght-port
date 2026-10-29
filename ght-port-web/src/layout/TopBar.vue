<template>
  <header class="topbar">
    <div class="crumb">
      <span class="crumb-home">港慧通</span>
      <el-icon class="sep"><ArrowRight /></el-icon>
      <span class="crumb-current">{{ route.meta.title }}</span>
    </div>
    <div class="right">
      <div class="live"><i />数字孪生 · 实时在线</div>
      <div class="clock num">{{ now }}</div>
      <div class="tenant">
        <el-icon><Location /></el-icon>
        {{ userStore.info?.tenantName }}
      </div>
      <el-dropdown trigger="click" @command="onCommand">
        <div class="user">
          <div class="avatar">{{ (userStore.info?.nickname || '用').slice(0, 1) }}</div>
          <div class="user-meta">
            <div class="name">{{ userStore.info?.nickname }}</div>
            <div class="role">{{ roleLabel }}</div>
          </div>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="pwd" icon="Lock">修改密码</el-dropdown-item>
            <el-dropdown-item command="logout" icon="SwitchButton" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form ref="pwdRef" :model="pwd" :rules="pwdRules" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword"><el-input v-model="pwd.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"><el-input v-model="pwd.newPassword" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPwd">确定</el-button>
      </template>
    </el-dialog>
  </header>
</template>

<script setup>
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/auth'
import { USER_TYPES, labelOf } from '@/utils/dict'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const now = ref(dayjs().format('MM-DD HH:mm:ss'))
const timer = setInterval(() => (now.value = dayjs().format('MM-DD HH:mm:ss')), 1000)
onBeforeUnmount(() => clearInterval(timer))

const roleLabel = computed(() => (userStore.isSuper ? '超级管理员' : labelOf(USER_TYPES, userStore.userType)))

const pwdVisible = ref(false)
const pwdRef = ref()
const pwd = reactive({ oldPassword: '', newPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码' }],
  newPassword: [{ required: true, min: 6, message: '新密码至少6位' }]
}
async function submitPwd() {
  await pwdRef.value.validate()
  await changePassword(pwd)
  ElMessage.success('密码已修改')
  pwdVisible.value = false
}

async function onCommand(cmd) {
  if (cmd === 'pwd') {
    pwd.oldPassword = ''
    pwd.newPassword = ''
    pwdVisible.value = true
    return
  }
  await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'info' })
  userStore.logout()
  router.replace('/login')
  setTimeout(() => location.reload(), 50)
}
</script>

<style scoped lang="scss">
.topbar {
  height: 64px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: saturate(160%) blur(10px);
  border-bottom: 1px solid var(--ght-line);
  position: sticky;
  top: 0;
  z-index: 5;
  &::after {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    bottom: -1px;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(47, 91, 211, 0.5) 30%, rgba(34, 195, 230, 0.6) 60%, transparent);
  }
}
.live {
  display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--ght-blue);
  padding: 4px 10px; border-radius: 999px; border: 1px solid var(--ght-tech-line); background: rgba(255, 255, 255, 0.7);
  i { width: 7px; height: 7px; border-radius: 50%; background: #22c55e; box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.6); animation: live 1.8s ease-out infinite; }
}
@keyframes live { 0% { box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.6); } 80%, 100% { box-shadow: 0 0 0 7px rgba(34, 197, 94, 0); } }
.crumb { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.crumb-home { color: var(--ght-ink-3); }
.sep { color: var(--ght-ink-4); font-size: 12px; }
.crumb-current { font-weight: 600; font-size: 16px; }
.right { display: flex; align-items: center; gap: 18px; }
.clock { color: var(--ght-ink-3); font-size: 13px; }
.tenant {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 12px; border-radius: 999px;
  background: var(--ght-blue-light); color: var(--ght-blue); font-weight: 500; font-size: 13px;
}
.user { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.avatar {
  width: 36px; height: 36px; border-radius: 12px;
  background: linear-gradient(135deg, #4f7fe8, #1e3a8a);
  color: #fff; display: grid; place-items: center; font-weight: 600;
}
.name { font-size: 14px; font-weight: 600; }
.role { font-size: 12px; color: var(--ght-ink-3); }
@media (max-width: 900px) {
  .clock, .user-meta, .live { display: none; }
}
</style>
