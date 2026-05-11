<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
</script>

<style>
:root {
  --primary-color: #4361ee;
  --primary-gradient: linear-gradient(135deg, #4361ee 0%, #4cc9f0 100%);
  --glass-bg: rgba(255, 255, 255, 0.7);
  --glass-border: rgba(255, 255, 255, 0.3);
  --glass-blur: blur(10px);
}

body {
  margin: 0;
  padding: 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #f8fafc;
  color: #1e293b;
  -webkit-font-smoothing: antialiased;
}

/* Glassmorphism Utility */
.glass-effect {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
}

/* Custom Scrollbar */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f5f9;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Global Component Overrides */
.el-button {
  font-weight: 600;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-button--primary:not(.is-link):not(.is-plain) {
  background: var(--primary-gradient) !important;
  border: none !important;
  color: #ffffff !important;
}

.el-button--primary:not(.is-link):not(.is-plain):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(67, 97, 238, 0.3);
}


.el-card {
  border-radius: 16px;
  border: 1px solid #f1f5f9 !important;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03) !important;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.el-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1) !important;
}

/* Premium Message & Notification Style */
.el-message {
  border-radius: 12px !important;
  border: none !important;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1) !important;
  backdrop-filter: blur(10px) !important;
  background-color: rgba(255, 255, 255, 0.9) !important;
  padding: 12px 20px !important;
}

.el-message--error {
  background-color: rgba(254, 242, 242, 0.95) !important;
}

.el-message--success {
  background-color: rgba(240, 253, 244, 0.95) !important;
}

.el-notification {
  border-radius: 16px !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1) !important;
  backdrop-filter: blur(15px) !important;
  background-color: rgba(255, 255, 255, 0.8) !important;
}

.el-notification__title {
  font-weight: 700 !important;
  color: #1e293b !important;
}

.el-notification__content {
  color: #64748b !important;
  line-height: 1.6 !important;
}

/* Center Table Columns Global Fallback */
.el-table .cell {
  text-align: center;
}
</style>

