import * as THREE from 'three'

/**
 * 河道中心线计算：控制点 (u,v) 为元素内归一化坐标，局部坐标原点在元素中心
 */

/** 缺省控制点：沿长边的 S 形 */
export function defaultPoints(w, h) {
  if (w >= h) return [{ u: 0, v: 0.5 }, { u: 0.33, v: 0.5 }, { u: 0.66, v: 0.5 }, { u: 1, v: 0.5 }]
  return [{ u: 0.5, v: 0 }, { u: 0.5, v: 0.33 }, { u: 0.5, v: 0.66 }, { u: 0.5, v: 1 }]
}

/** 控制点（局部坐标） */
export function controlPoints(el) {
  const pts = el.cfg?.points && el.cfg.points.length >= 2 ? el.cfg.points : defaultPoints(el.width, el.height)
  return pts.map((p) => new THREE.Vector3(p.u * el.width - el.width / 2, 0, p.v * el.height - el.height / 2))
}

/** 河宽 */
export function riverWidth(el) {
  return Number(el.cfg?.riverWidth) || Math.min(el.width, el.height)
}

/**
 * 采样中心线
 * @returns {{pts: THREE.Vector3[], nrm: THREE.Vector3[], len: number[]}} 采样点、左法线、累计长度
 */
export function sampleCenterline(el) {
  const cps = controlPoints(el)
  const curve = new THREE.CatmullRomCurve3(cps, false, 'centripetal')
  const total = curve.getLength()
  const n = Math.max(24, Math.min(420, Math.round(total / 6)))
  const pts = curve.getSpacedPoints(n)
  const nrm = []
  const len = [0]
  for (let i = 0; i < pts.length; i++) {
    const a = pts[Math.max(0, i - 1)]
    const b = pts[Math.min(pts.length - 1, i + 1)]
    const t = b.clone().sub(a).normalize()
    nrm.push(new THREE.Vector3(-t.z, 0, t.x))
    if (i > 0) len.push(len[i - 1] + pts[i].distanceTo(pts[i - 1]))
  }
  return { pts, nrm, len }
}

/** 局部坐标 -> 世界坐标（xz 平面） */
export function localToWorld(el, W, H, p) {
  const r = (-(el.rotation || 0) * Math.PI) / 180
  const cx = el.posX + el.width / 2 - W / 2
  const cz = el.posY + el.height / 2 - H / 2
  return { x: cx + p.x * Math.cos(r) + p.z * Math.sin(r), z: cz - p.x * Math.sin(r) + p.z * Math.cos(r) }
}

/** 河道世界坐标信息（用于交汇处裁剪河岸、泊位朝向） */
export function worldInfo(el, W, H) {
  const { pts } = sampleCenterline(el)
  return { uid: el.uid, half: riverWidth(el) / 2, pts: pts.map((p) => localToWorld(el, W, H, p)) }
}

/** 点到折线最近距离及最近点 */
export function nearestOnPolyline(pts, x, z) {
  let best = { d: Infinity, x: 0, z: 0 }
  for (let i = 0; i < pts.length - 1; i++) {
    const a = pts[i]
    const b = pts[i + 1]
    const dx = b.x - a.x
    const dz = b.z - a.z
    const l2 = dx * dx + dz * dz || 1
    const t = Math.max(0, Math.min(1, ((x - a.x) * dx + (z - a.z) * dz) / l2))
    const px = a.x + dx * t
    const pz = a.z + dz * t
    const d = Math.hypot(x - px, z - pz)
    if (d < best.d) best = { d, x: px, z: pz }
  }
  return best
}
