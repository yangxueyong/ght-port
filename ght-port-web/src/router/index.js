import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/storage'
import { buildMenuRoutes, firstMenuPath } from './dynamic'

const Layout = () => import('@/layout/MainLayout.vue')

const staticRoutes = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue'), meta: { public: true, title: '登录' } },
  { path: '/pass/:token', name: 'DriverPass', component: () => import('@/views/gatepass/public.vue'), meta: { public: true, title: '电子出门条' } },
  { path: '/gate/verify/:token', name: 'GateVerify', component: () => import('@/views/gatepass/verify.vue'), meta: { title: '出门条核销' } },
  { path: '/403', name: 'Forbidden', component: () => import('@/views/error/403.vue'), meta: { public: true, title: '无权限' } },
  { path: '/', name: 'Root', component: Layout, children: [] }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes: staticRoutes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach(async (to) => {
  document.title = (to.meta.title ? to.meta.title + ' · ' : '') + '港慧通'
  if (to.meta.public) return true
  if (!getToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  const store = useUserStore()
  if (!store.routesReady) {
    try {
      await store.fetchInfo()
    } catch (e) {
      store.logout()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    buildMenuRoutes(store.menus).forEach((r) => router.addRoute('Root', r))
    router.addRoute({ path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/error/404.vue'), meta: { title: '页面不存在' } })
    store.routesReady = true
    if (to.path === '/') {
      return { path: firstMenuPath(store.menus) || '/403', replace: true }
    }
    return { path: to.path, query: to.query, hash: to.hash, replace: true }
  }
  if (to.path === '/') {
    return { path: firstMenuPath(store.menus) || '/403', replace: true }
  }
  return true
})

export default router
