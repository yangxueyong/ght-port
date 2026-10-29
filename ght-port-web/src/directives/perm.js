import { useUserStore } from '@/stores/user'

/**
 * v-perm="'carrier:edit'" 或 v-perm="['a','b']"：无权限时移除元素
 */
export function setupPermissionDirective(app) {
  app.directive('perm', {
    mounted(el, binding) {
      const store = useUserStore()
      if (!store.hasPerm(binding.value)) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  })
}
