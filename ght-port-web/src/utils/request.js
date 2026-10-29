import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearToken } from './storage'
import { API_BASE_URL } from '@/config'

const service = axios.create({
  baseURL: API_BASE_URL,
  timeout: 20000
})

service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

let redirecting = false

service.interceptors.response.use(
  (resp) => {
    const res = resp.data
    if (res.code === 200) {
      return res.data
    }
    if (res.code === 401) {
      clearToken()
      if (!redirecting && !location.hash.startsWith('#/login')) {
        redirecting = true
        ElMessage.warning(res.msg || '登录已过期')
        const redirect = encodeURIComponent(location.hash.replace(/^#/, ''))
        location.hash = '#/login?redirect=' + redirect
        setTimeout(() => (redirecting = false), 1000)
      }
      return Promise.reject(new Error(res.msg))
    }
    if (!resp.config.silent) {
      ElMessage.error(res.msg || '请求失败')
    }
    return Promise.reject(new Error(res.msg))
  },
  (err) => {
    ElMessage.error(err.message?.includes('timeout') ? '请求超时，请稍后再试' : '网络异常，请检查服务是否启动')
    return Promise.reject(err)
  }
)

/** 统一使用 POST（后端接口均为 POST） */
export function post(url, data = {}, config = {}) {
  return service.post(url, data, config)
}

export default service
