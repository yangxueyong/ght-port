const TOKEN_KEY = 'ght_token'
const TENANT_KEY = 'ght_tenant'

/** 安全读取本地存储（隐私模式等场景可能抛异常） */
export function getItem(key, fallback = null) {
  try {
    const v = localStorage.getItem(key)
    return v === null ? fallback : v
  } catch (e) {
    return fallback
  }
}

export function setItem(key, value) {
  try {
    localStorage.setItem(key, value)
  } catch (e) {
    /* ignore */
  }
}

export function removeItem(key) {
  try {
    localStorage.removeItem(key)
  } catch (e) {
    /* ignore */
  }
}

export const getToken = () => getItem(TOKEN_KEY)
export const setToken = (t) => setItem(TOKEN_KEY, t)
export const clearToken = () => removeItem(TOKEN_KEY)
export const getLastTenant = () => getItem(TENANT_KEY)
export const setLastTenant = (t) => setItem(TENANT_KEY, t)
