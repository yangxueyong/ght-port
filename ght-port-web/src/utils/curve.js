/** 点到线段距离的平方，用于双击时判断插入位置 */
export function segDist2(px, py, a, b) {
  const vx = b.x - a.x
  const vy = b.y - a.y
  const len = vx * vx + vy * vy || 1
  let t = ((px - a.x) * vx + (py - a.y) * vy) / len
  t = Math.max(0, Math.min(1, t))
  const dx = a.x + t * vx - px
  const dy = a.y + t * vy - py
  return dx * dx + dy * dy
}
