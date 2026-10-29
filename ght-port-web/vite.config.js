import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  // 读取 .env.development / .env.production 中的配置
  const env = loadEnv(mode, process.cwd())
  const apiBase = env.VITE_API_BASE || '/api'
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    css: {
      preprocessorOptions: {
        scss: { api: 'modern-compiler' }
      }
    },
    server: {
      host: '0.0.0.0',
      port: Number(env.VITE_PORT) || 5173,
      proxy: {
        [apiBase]: {
          target: env.VITE_PROXY_TARGET || 'http://localhost:18080',
          changeOrigin: true
        }
      }
    }
  }
})
