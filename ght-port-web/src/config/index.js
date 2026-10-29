/**
 * 前端统一配置入口
 * 优先级：public/config.js（运行时，可在打包后修改） > .env.[mode] 中的 VITE_API_BASE > 默认 /api
 */
const runtime = (typeof window !== 'undefined' && window.GHT_CONFIG) || {}

export const API_BASE_URL = runtime.apiBaseUrl || import.meta.env.VITE_API_BASE || '/api'

export default {
  apiBaseUrl: API_BASE_URL
}
