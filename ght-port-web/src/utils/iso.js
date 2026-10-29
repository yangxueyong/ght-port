import { shade } from './color'

/**
 * 斜二测（近似 3D）投影工具：
 * 侧视图坐标 (x, y) 加上纵深 z，投影为 (x + z * DX, y - z * DY)
 */
export const DX = 0.62
export const DY = 0.42

/** 投影一个三维点（x 向右、y 向下、z 向纸面内） */
export const proj = (x, y, z) => [x + z * DX, y - z * DY]

const pts = (arr) => arr.map((p) => p.join(',')).join(' ')

/**
 * 生成长方体的三个可见面（前侧面、顶面、右端面）
 * @param {number} x 左下前角 x
 * @param {number} y 顶部 y（侧视图）
 * @param {number} w 长度
 * @param {number} h 高度
 * @param {number} d 纵深
 * @param {string} color 基础色
 * @param {number} z0 起始纵深（默认 0）
 */
export function box(x, y, w, h, d, color, z0 = 0) {
  const a = proj(x, y, z0)
  const b = proj(x + w, y, z0)
  const c = proj(x + w, y + h, z0)
  const e = proj(x, y + h, z0)
  const at = proj(x, y, z0 + d)
  const bt = proj(x + w, y, z0 + d)
  const ct = proj(x + w, y + h, z0 + d)
  return [
    { points: pts([a, b, c, e]), fill: color },
    { points: pts([a, b, bt, at]), fill: shade(color, 0.28) },
    { points: pts([b, bt, ct, c]), fill: shade(color, -0.22) }
  ]
}

/** 投影多边形（[x, y, z] 数组）为 points 字符串 */
export const poly = (list) => pts(list.map(([x, y, z]) => proj(x, y, z)))

/**
 * 俯视图中的“抬升立方体”：顶面整体上移 hgt，南侧露出立面，形成 2.5D 立体感
 * @returns {{top: string, front: string, topColor: string, frontColor: string}}
 */
export function tbox(x, y, w, d, hgt, color) {
  return {
    top: pts([[x, y - hgt], [x + w, y - hgt], [x + w, y + d - hgt], [x, y + d - hgt]]),
    front: pts([[x, y + d - hgt], [x + w, y + d - hgt], [x + w, y + d], [x, y + d]]),
    topColor: color,
    frontColor: shade(color, -0.28)
  }
}

/** 倒角矩形（硬表面切角），返回 points 字符串 */
export function chamfer(x, y, w, h, c = 6) {
  const k = Math.min(c, w / 3, h / 3)
  return pts([[x + k, y], [x + w - k, y], [x + w, y + k], [x + w, y + h - k], [x + w - k, y + h], [x + k, y + h], [x, y + h - k], [x, y + k]])
}

/** 等距取点：在 [from, to] 区间内按步长生成坐标 */
export function steps(from, to, step) {
  const arr = []
  for (let v = from; v <= to; v += step) arr.push(v)
  return arr
}
