/** 颜色明暗调整：amt 为 -1~1，负数变暗、正数变亮 */
export function shade(hex, amt) {
  let c = (hex || '#1f7ae0').replace('#', '')
  if (c.length === 3) c = c.split('').map((x) => x + x).join('')
  const n = parseInt(c, 16)
  let r = (n >> 16) & 255
  let g = (n >> 8) & 255
  let b = n & 255
  const t = amt < 0 ? 0 : 255
  const p = Math.abs(amt)
  r = Math.round((t - r) * p + r)
  g = Math.round((t - g) * p + g)
  b = Math.round((t - b) * p + b)
  return '#' + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1)
}

let uidSeed = 0
/** 生成 SVG 内部 id，避免多个实例的渐变/图案互相覆盖 */
export const uid = (p = 'g') => `${p}${++uidSeed}`
