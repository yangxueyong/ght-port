import dayjs from 'dayjs'

export const fmtTime = (v, p = 'YYYY-MM-DD HH:mm') => (v ? dayjs(v).format(p) : '-')
export const fmtNum = (v, digits = 2) => {
  if (v === null || v === undefined || v === '') return '0'
  const n = Number(v)
  if (Number.isNaN(n)) return v
  return n.toLocaleString('zh-CN', { maximumFractionDigits: digits })
}
export const fromNow = (v) => {
  if (!v) return ''
  const diff = dayjs().diff(dayjs(v), 'minute')
  if (diff < 1) return '刚刚'
  if (diff < 60) return diff + ' 分钟前'
  if (diff < 1440) return Math.floor(diff / 60) + ' 小时前'
  return Math.floor(diff / 1440) + ' 天前'
}
export const percent = (part, total) => {
  const t = Number(total) || 0
  if (t <= 0) return 0
  return Math.min(100, Math.round((Number(part) / t) * 100))
}
