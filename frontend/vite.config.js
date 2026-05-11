import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 3000,
    proxy: {
      '/api': {
        target: process.env.BACKEND_URL || 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: process.env.BACKEND_URL || 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws-chat': {
        target: process.env.BACKEND_URL || 'http://localhost:8080',
        ws: true,
        changeOrigin: true
      }
    },
    watch: {
      usePolling: true
    }
  }
})
