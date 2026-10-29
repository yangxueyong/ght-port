/** 驾驶舱图表配色（已通过色盲安全校验）：卸货蓝、装货琥珀、车辆运出青 */
export const SERIES = {
  unload: { label: '卸货', color: '#2563eb' },
  load: { label: '装货', color: '#d97706' },
  trip: { label: '车辆运出', color: '#0d9488' }
}

/** 泊位状态色（状态专用，始终配合文字） */
export const STATUS = {
  IDLE: { label: '未作业', color: '#12b886' },
  WORKING: { label: '正在作业', color: '#f59f00' },
  MAINTENANCE: { label: '维修中', color: '#e5484d' }
}

/** 紧凑数字：万以上显示为“x.x万” */
export function compact(v) {
  const n = Number(v) || 0
  if (Math.abs(n) >= 10000) return (n / 10000).toLocaleString('zh-CN', { maximumFractionDigits: 1 }) + '万'
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 1 })
}
