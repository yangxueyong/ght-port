import { defineStore } from 'pinia'
import { getToken, setToken, clearToken, setLastTenant } from '@/utils/storage'
import { login as loginApi, userInfo } from '@/api/auth'

const ALL = '*:*:*'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    info: null,
    menus: [],
    perms: [],
    roles: [],
    routesReady: false
  }),
  getters: {
    isSuper: (s) => s.roles.includes('SUPER_ADMIN'),
    userType: (s) => s.info?.userType
  },
  actions: {
    async login(form) {
      const res = await loginApi(form)
      this.token = res.token
      setToken(res.token)
      setLastTenant(form.tenantCode)
      return res
    },
    async fetchInfo() {
      const res = await userInfo()
      this.info = res
      this.menus = res.menus || []
      this.perms = res.perms || []
      this.roles = res.roles || []
      return res
    },
    hasPerm(value) {
      if (!value) return true
      if (this.isSuper || this.perms.includes(ALL)) return true
      const list = Array.isArray(value) ? value : [value]
      return list.some((p) => this.perms.includes(p))
    },
    logout() {
      this.token = null
      this.info = null
      this.menus = []
      this.perms = []
      this.roles = []
      this.routesReady = false
      clearToken()
    }
  }
})
