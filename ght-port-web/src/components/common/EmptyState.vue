<template>
  <div class="empty">
    <svg viewBox="0 0 200 120" width="180" height="108">
      <defs>
        <linearGradient id="emptyWater" x1="0" x2="0" y1="0" y2="1">
          <stop offset="0" stop-color="#cfe3fb" />
          <stop offset="1" stop-color="#eaf3fe" />
        </linearGradient>
      </defs>
      <rect x="10" y="78" width="180" height="30" rx="15" fill="url(#emptyWater)" />
      <path d="M50 78 h80 l-10 14 h-60z" fill="#b7c7e6" />
      <rect x="70" y="58" width="34" height="20" rx="3" fill="#d5def0" />
      <rect x="108" y="64" width="16" height="14" rx="2" fill="#e3e9f5" />
      <path d="M20 96 q10 -6 20 0 t20 0 t20 0 t20 0 t20 0 t20 0 t20 0 t20 0" stroke="#a9c8f1" stroke-width="2" fill="none">
        <animateTransform attributeName="transform" type="translate" values="0 0;-20 0;0 0" dur="4s" repeatCount="indefinite" />
      </path>
    </svg>
    <div class="text">{{ text }}</div>
    <slot />
  </div>
</template>

<script setup>
defineProps({ text: { type: String, default: '暂无数据' } })
</script>

<style scoped>
.empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40px 0; color: var(--ght-ink-3); gap: 10px; }
.text { font-size: 13px; }
</style>
