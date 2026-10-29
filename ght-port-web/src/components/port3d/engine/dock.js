import { worldInfo, nearestOnPolyline } from './river.js'

const SNAP_RANGE = 280
const FRONT_INTO_WATER = 4
const ROT_STEP = 5

/**
 * 泊位自动靠岸：找到最近的河道，使泊位临水侧（局部 -z）朝向河道，并将岸壁贴齐河岸
 * 河道两岸均可停靠：泊位在哪一侧就朝向哪一侧
 * @param {Object} el 泊位模型
 * @param {Array} elements 全部元素
 * @param {number} W 画布宽
 * @param {number} H 画布高
 * @returns {{posX:number, posY:number, rotation:number}|null} 无可停靠河道时返回 null
 */
export function dockToRiver(el, elements, W, H) {
  const rivers = elements.filter((e) => e.elementType === 'RIVER')
  if (!rivers.length) return null
  const cx = el.posX + el.width / 2 - W / 2
  const cz = el.posY + el.height / 2 - H / 2
  let best = null
  rivers.forEach((r) => {
    const info = worldInfo(r, W, H)
    const n = nearestOnPolyline(info.pts, cx, cz)
    const gap = n.d - info.half
    if (!best || gap < best.gap) best = { ...n, half: info.half, gap }
  })
  if (!best || best.gap > SNAP_RANGE) return null
  let dx = best.x - cx
  let dz = best.z - cz
  const len = Math.hypot(dx, dz) || 1
  dx /= len
  dz /= len
  const deg = (Math.atan2(dx, -dz) * 180) / Math.PI
  const dist = best.half - FRONT_INTO_WATER + el.height / 2
  const ncx = best.x - dx * dist
  const ncz = best.z - dz * dist
  return {
    posX: Math.round(ncx + W / 2 - el.width / 2),
    posY: Math.round(ncz + H / 2 - el.height / 2),
    rotation: Math.round(deg / ROT_STEP) * ROT_STEP
  }
}
